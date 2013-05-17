package controllers.website

import com.bindscha.playmate.boilerplate.controllers._

/**
 * Assets used in the application.
 *
 * <br />
 *
 * @author Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 * @maintainer Laurent Bindschaedler <b>laurent.bindschaedler@epfl.ch</b>
 */
trait ApplicationAssets extends com.bindscha.playmate.boilerplate.controllers.Assets {

  import play.api.Logger
  import play.api.templates.Html

  import com.bindscha.playmate.boilerplate.views.html

  val D3 = "d3"
  val HANDLEBARS = "handlebars"
  val EMBER = "ember"
  val EMBER_DATA = "ember-data"

  val D3_VERSION = "3.0.6"
  val HANDLEBARS_VERSION = "1.0.rc.2"
  val EMBER_VERSION = "1.0.0-pre.4"
  val EMBER_DATA_VERSION = "snapshot"

  private val APPLICATION_ASSETS_ROOT = "/assets"

  private val ASSETS_MAP: Map[Asset, Html] = Map(
    Favicon ->
      html.helpers.icon("shortcut icon", s"$APPLICATION_ASSETS_ROOT/icn/favicon.ico"),
    AppleIcon57 ->
      html.helpers.icon("apple-touch-icon-precomposed", s"$APPLICATION_ASSETS_ROOT/icn/apple-touch-icon-57x57.png"),
    AppleIcon72 ->
      html.helpers.icon("apple-touch-icon-precomposed", s"$APPLICATION_ASSETS_ROOT/icn/apple-touch-icon-72x72.png", Some("72x72")),
    AppleIcon114 ->
      html.helpers.icon("apple-touch-icon-precomposed", s"$APPLICATION_ASSETS_ROOT/icn/apple-touch-icon-114x114.png", Some("114x114")),
    AppleIcon144 ->
      html.helpers.icon("apple-touch-icon-precomposed", s"$APPLICATION_ASSETS_ROOT/icn/apple-touch-icon-144x144.png", Some("144x144")),
    Script(D3) ->
      html.helpers.script(
        s"$APPLICATION_ASSETS_ROOT/js/$D3-$D3_VERSION.js",
        Some(s"$APPLICATION_ASSETS_ROOT/js/$D3-$D3_VERSION.min.js"),
        Some(s"//cdnjs.cloudflare.com/ajax/libs/d3/$D3_VERSION/$D3.v2.min.js"),
        Some("(typeof d3.version == 'string')")),
    Script(HANDLEBARS) ->
      html.helpers.script(
        s"$APPLICATION_ASSETS_ROOT/js/$HANDLEBARS-$HANDLEBARS_VERSION.js",
        Some(s"$APPLICATION_ASSETS_ROOT/js/$HANDLEBARS-$HANDLEBARS_VERSION.min.js"),
        Some(s"//cdnjs.cloudflare.com/ajax/libs/handlebars.js/$HANDLEBARS_VERSION/$HANDLEBARS.min.js"),
        Some("(typeof Handlebars.VERSION == 'string')")),
    Script(EMBER) ->
      html.helpers.script(
        s"$APPLICATION_ASSETS_ROOT/js/$EMBER-$EMBER_VERSION.js",
        Some(s"$APPLICATION_ASSETS_ROOT/js/$EMBER-$EMBER_VERSION.min.js"),
        Some(s"//cdnjs.cloudflare.com/ajax/libs/ember.js/$EMBER_VERSION/$EMBER-$EMBER_VERSION.min.js"),
        Some("(typeof Ember.VERSION == 'string')")),
    Script(EMBER_DATA) ->
      html.helpers.script(
        s"$APPLICATION_ASSETS_ROOT/js/$EMBER_DATA-$EMBER_DATA_VERSION.js",
        Some(s"$APPLICATION_ASSETS_ROOT/js/$EMBER_DATA-$EMBER_DATA_VERSION.min.js"),
        None, // TODO: use CDN when possible
        Some("(typeof DS.Store == 'function')"))
  )

  abstract override def apply(asset: Asset): Html =
    ASSETS_MAP.applyOrElse(asset, super.apply _)

  abstract override def isDefinedAt(asset: Asset): Boolean =
    ASSETS_MAP.contains(asset) || super.isDefinedAt(asset)

}

object ApplicationAssets
  extends EmptyAssets
  with BoilerplateAssets
  with ApplicationAssets

trait ApplicationAssetsController
  extends AssetsController
  with EmptyAssets
  with BoilerplateAssets
  with ApplicationAssets

object ApplicationAssetsController extends ApplicationAssetsController
