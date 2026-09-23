package com.devonfw.tools.ide.url.tool.gemini;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.devonfw.tools.ide.url.model.folder.UrlRepository;
import com.devonfw.tools.ide.url.updater.AbstractUrlUpdaterTest;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@WireMockTest
public class GeminiUrlUpdaterTest extends AbstractUrlUpdaterTest {

  @Test
  void testGeminiUrlUpdater(@TempDir Path tempDir, WireMockRuntimeInfo wmRuntimeInfo) {

    stubFor(get(urlMatching("/repos/google-gemini/gemini-cli/releases"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(readAndResolve(PATH_INTEGRATION_TEST.resolve("GeminiUrlUpdater")
                .resolve("gemini-release.json"), wmRuntimeInfo))));

    stubFor(any(urlMatching(
        "/google-gemini/gemini-cli/releases/download/v[\\w.-]+/gemini-darwin-(x64|arm64)(-unsigned)?\\.zip"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(DOWNLOAD_CONTENT)));

    UrlRepository urlRepository = UrlRepository.load(tempDir);
    GeminiUrlUpdater updater = new GeminiUrlUpdater(wmRuntimeInfo.getHttpBaseUrl(), wmRuntimeInfo.getHttpBaseUrl());

    // act
    update(updater, urlRepository);

    // assert
    List<String> expectedPlatforms = List.of("windows_x64", "windows_arm64");
    Path geminiDir = tempDir.resolve("gemini").resolve("gemini");
    assertUrlVersion(geminiDir.resolve("v0.62.0"), expectedPlatforms);
  }
}
