package lila.puzzle

case class PuzzleSetItem(
    id: String,
    setName: String,
    user: String,
    puzzle: Puzzle
)

object PuzzleSetItem:

  val idSep = ':'

  case class Id(userId: UserId, puzzleId: PuzzleId):
    override def toString = s"${userId}$idSep${puzzleId}"

  object BSONFields:
    val id       = "_id"
    val setName  = "n"
    val user     = "u"

  import lila.db.dsl.*
  def puzzleLookup(colls: PuzzleColls, pipeline: List[Bdoc] = Nil) =
    $lookup.pipelineFull(
      from = colls.puzzle.name.value,
      as = "puzzle",
      let = $doc("pid" -> $doc("$arrayElemAt" -> $arr($doc("$split" -> $arr("$_id", ":")), 1))),
      pipe = $doc("$match" -> $expr($doc("$eq" -> $arr("$_id", "$$pid")))) :: pipeline
    )