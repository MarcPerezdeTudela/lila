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

  final class PuzzleSetCollectionAdapter(user: User, colls: PuzzleColls)(using Executor)
      extends AdapterLike[PuzzleSet]:

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
              PipelineOperator(
                $doc(
                  "$lookup" -> $doc(
                    "from"         -> colls.puzzle.name.value,
                    "localField"   -> "p",
                    "foreignField" -> "_id",
                    "as"           -> "puzzles"
                  )
                )
              )
            )
          }
        }
        .map { r =>
          for {
            doc     <- r
            id      <- doc.getAsOpt[String](PuzzleSet.BSONFields.id)
            name    <- doc.getAsOpt[String](PuzzleSet.BSONFields.name)
            user    <- doc.getAsOpt[String](PuzzleSet.BSONFields.user)
            puzzles <- doc.getAsOpt[List[Puzzle]]("puzzles")
          } yield PuzzleSet(id, name, user, puzzles)
        }

final class PuzzleSetCollectionApi(colls: PuzzleColls)(using Executor):

  import PuzzleSetCollection.*

  def apply(user: User, page: Int): Fu[Paginator[PuzzleSet]] =
    Paginator[PuzzleSet](
      new PuzzleSetCollectionAdapter(user, colls),
      currentPage = page,
      maxPerPage = maxPerPage
    )
