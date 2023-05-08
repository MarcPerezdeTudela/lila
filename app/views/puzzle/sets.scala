package views
package html.puzzle

import controllers.routes

import lila.api.Context
import lila.app.templating.Environment.{ given, * }
import lila.app.ui.ScalatagsTemplate.{ *, given }
import lila.common.paginator.Paginator
import lila.puzzle.PuzzleHistory.{ PuzzleSession, SessionRound }
import lila.puzzle.PuzzleTheme
import lila.user.User

object sets:

  def apply(user: User)(using ctx: Context) =
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
            )
          )
        )
      )
    )
