package com.devonfw.tools.ide.url.tool.gemini;

import com.devonfw.tools.ide.url.model.folder.UrlVersion;
import com.devonfw.tools.ide.url.updater.GithubUrlReleaseUpdater;

public class GeminiUrlUpdater extends GithubUrlReleaseUpdater {

  public GeminiUrlUpdater() {
    super();
  }

  GeminiUrlUpdater(String downloadBaseUrl, String versionBaseUrl) {
    super(downloadBaseUrl, versionBaseUrl);
  }

  @Override
  public String getTool() {
    return "gemini";
  }

  @Override
  protected String getGithubOrganization() {
    return "google-gemini";
  }

  @Override
  protected String getGithubRepository() {
    return "gemini-cli";
  }

  @Override
  protected void addVersion(UrlVersion urlVersion) {
    String baseUrl = createGithubReleaseDownloadUrl("${version}", "");

    doAddVersion(urlVersion, baseUrl + "gemini-darwin-x64-unsigned", WINDOWS, X64);
    doAddVersion(urlVersion, baseUrl + "gemini-darwin-arm64-unsigned", WINDOWS, ARM64);
  }

  @Override
  public String getCpeVendor() {
    return "google";
  }

  @Override
  public String getCpeProduct() {
    return "gemini-cli";
  }
}
