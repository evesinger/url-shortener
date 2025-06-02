package com.example.urlshortener.api;

import com.example.urlshortener.application.ShortenerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortenerController.class)
class ShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortenerService shortenerService;


    @Test
    void shortenUrl_returns_shortened_url_and_stats() throws Exception {
        ShortUrlDto mockResponse = new ShortUrlDto("short.ly/abc123", "https://example.com", 1, 0);
        Mockito.when(shortenerService.shortenUrl(anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "https://example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("short.ly/abc123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.usedCount").value(0))
                .andExpect(jsonPath("$.requestCount").value(1));
    }

    @Test
    void shouldReturn400WhenUrlIsMissing() throws Exception {
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // no url field
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUrlIsBlank() throws Exception {
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "url": ""
                                    }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUrlIsInvalid() throws Exception {
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "url": "not-a-valid-url"
                                    }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirectToOriginalUrl() throws Exception {
        Mockito.when(shortenerService.getOriginalUrl(anyString())).thenReturn(Optional.of("https://example.com"));

        mockMvc.perform(get("/short.ly/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    assert Objects.equals(location, "https://example.com");
                });

        verify(shortenerService).incrementUsedCount("abc123");
    }

    @Test
    void redirectToOriginalUrl_returns404_when_no_originalUrl() throws Exception {
        Mockito.when(shortenerService.getOriginalUrl(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/short.ly/abc123"))
                .andExpect(status().isNotFound());

        verify(shortenerService, never()).incrementUsedCount(anyString());
    }

    @Test
    void getStatistics_returns_stats() throws Exception {
        ShortUrlDto mockResponse = new ShortUrlDto("short.ly/abc123", "https://example.com", 1, 0);
        Mockito.when(shortenerService.getStatistics(anyString())).thenReturn(mockResponse);

        mockMvc.perform(get("/stats/short.ly/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("short.ly/abc123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.usedCount").value(0))
                .andExpect(jsonPath("$.requestCount").value(1));
    }
}