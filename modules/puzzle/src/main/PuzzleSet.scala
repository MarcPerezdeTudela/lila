package lila.puzzle

case class PuzzleSet(
    id: PuzzleSet.Id,
    name: SetName,
    user: UserName,
    puzzles: List[Puzzle]
)

object PuzzleSet:

  val idSep = ':'

  case class Id(userId: UserId, setName: String):
    override def toString = s"${userId}$idSep${setName}"

  object BSONFields:
    val id      = "_id"
    val name    = "n"
    val user    = "u"
    val puzzles = "p"
