import scala.sys.process._

val lastVersionString = "git tag -l".!!.split("\r?\n").last

version in Paradox := {
  if (isSnapshot.value)
    lastVersionString
  else version.value
}

paradoxProperties += ("app-version" -> {
  if (isSnapshot.value)
    lastVersionString
  else version.value
})

enablePlugins(ParadoxSitePlugin, ParadoxMaterialThemePlugin)

sourceDirectory in Paradox := sourceDirectory.value / "main" / "paradox"

ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox)

paradoxMaterialTheme in Paradox ~= {
  _.withLogoIcon("desktop_mac")
    .withCopyright("© QuadStingray 2018")
    .withColor("teal", "indigo")
    .withRepository(uri("https://github.com/QuadStingray/google-cloud-logging"))
}

paradoxMaterialTheme in Paradox ~= {
  _.withRepository(uri("https://github.com/QuadStingray/google-cloud-logging"))
}

paradoxMaterialTheme in Compile ~= {
  _.withLogoIcon("cloud")
    .withCopyright("© QuadStingray 2018")
    .withColor("teal", "indigo")
}

enablePlugins(SiteScaladocPlugin)

enablePlugins(GhpagesPlugin)

git.remoteRepo := "git@github.com:QuadStingray/google-cloud-logging.git"

ghpagesNoJekyll := true