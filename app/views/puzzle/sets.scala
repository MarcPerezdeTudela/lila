package views
package html.puzzle

import controllers.routes

import lila.api.Context
import lila.app.templating.Environment.{ given, * }
import lila.app.ui.ScalatagsTemplate.{ *, given }
import lila.common.paginator.Paginator
import lila.puzzle.PuzzleSetCollection.{ PuzzleSet }
import lila.puzzle.Puzzle
import lila.user.User

object sets:

  def apply(user: User, pager: Paginator[PuzzleSet])(using ctx: Context) =
    val title =
      if (ctx is user) trans.puzzle.sets.txt()
      else s"${user.username} ${trans.puzzle.sets.txt()}"
    views.html.base.layout(
      title = title,
      moreCss = cssTag("puzzle.dashboard"),
      moreJs = infiniteScrollTag
    )(
      main(cls := "page-menu")(
        bits.pageMenu("sets", user.some),
        div(cls := "page-menu__content box box-pad")(
          h1(cls := "box__top")(title),
          div(cls := "puzzle-sets")(
            div(cls := "infinite-scroll")(
              pager.currentPageResults map renderSet,
            )
          )
        )
      )
    )

  private def renderSet(set: PuzzleSet)(implicit ctx: Context) =
      div(cls := "puzzle-sets__set")(
        h2(cls := "puzzle-sets__set__title")(
          strong(set.name),
        ),
        div(cls := "puzzle-sets__set__rounds")(renderPuzzle(set.items.head.puzzle))
      )
  private def renderPuzzle(puzzle: Puzzle)(implicit ctx: Context) =
    a(cls := "puzzle-history__round", href := routes.Puzzle.show(puzzle.id))(
      views.html.board.bits.mini(puzzle.fenAfterInitialMove.board, puzzle.color, puzzle.line.head.some)(
        span(cls := "puzzle-history__round__puzzle")
      ),
    )