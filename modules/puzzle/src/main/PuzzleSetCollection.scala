package lila.puzzle

import cats.data.NonEmptyList
import reactivemongo.api.ReadPreference

import lila.common.config.MaxPerPage
import lila.common.paginator.{ AdapterLike, Paginator }
import lila.db.dsl.{ *, given }
import lila.user.User
import scala.concurrent.java8.FuturesConvertersImpl.P
import alleycats.std.set

object PuzzleSetCollection:

    val maxPerPage = MaxPerPage(100)

    case class PuzzleSet(
        name: String,
        items: NonEmptyList[PuzzleSetItem]
    )

    final class PuzzleSetCollectionAdapter(user: User, colls: PuzzleColls)(using Executor) extends AdapterLike[PuzzleSet]:

        import BsonHandlers.given

        def nbResults: Fu[Int] = fuccess(user.perfs.puzzle.nb)

        def slice(offset: Int, length: Int): Fu[Seq[PuzzleSet]] =
            colls
                .set {
                    _.aggregateList(length, readPreference = ReadPreference.secondaryPreferred) { framework =>
                        import framework.*
                        Match($doc("u" -> user.id)) -> List(
                            Skip(offset),
                            Limit(length),
                            PipelineOperator(PuzzleSetItem puzzleLookup colls),
                            Unwind("puzzle")
                        )
                    }
                }
                .map { r =>
                    for {
                        doc    <- r
                        id     <- doc.getAsOpt[String](PuzzleSetItem.BSONFields.id)
                        setName   <- doc.getAsOpt[String](PuzzleSetItem.BSONFields.setName)
                        user   <- doc.getAsOpt[String](PuzzleSetItem.BSONFields.user)
                        puzzle <- doc.getAsOpt[Puzzle]("puzzle")

                        _ = println(r)
                        _ = println("Id: " + id)
                        _ = println("SetName: " + setName)
                        _ = println("User: " + user)
                        _ = println("PuzzleId: " + puzzle.id)
                    } yield PuzzleSetItem(id, setName, user, puzzle)
                }.map(groupBySet)
                

    def groupBySet(setItems: List[PuzzleSetItem]): List[PuzzleSet] =
            setItems
                .groupBy(_.setName).map { (setName, items) =>
                    PuzzleSet(setName, NonEmptyList.fromListUnsafe(items))
                }.toList
    

final class PuzzleSetCollectionApi(colls: PuzzleColls)(using Executor):

    import PuzzleSetCollection.*

    def apply(user: User, page: Int): Fu[Paginator[PuzzleSet]] =
        Paginator[PuzzleSet](
            new PuzzleSetCollectionAdapter(user, colls),
            currentPage = page,
            maxPerPage = maxPerPage
        )