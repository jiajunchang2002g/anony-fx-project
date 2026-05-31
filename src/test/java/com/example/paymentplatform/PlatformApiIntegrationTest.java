package com.example.paymentplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.paymentplatform.entity.Wallet;
import com.example.paymentplatform.repository.WalletRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private WalletRepository walletRepository;

  @Test
  void authWalletTransferAndLedgerFlowWorks() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
      "email" : "alice@example.com", "password" : "Password123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode registerBody = objectMapper.readTree(registerResponse);
        String token = registerBody.path("data").path("token").asText();
        assertThat(token).isNotBlank();

        String sgdWalletResponse = mockMvc.perform(post("/api/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
      "currency" : "SGD"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String usdWalletResponse = mockMvc.perform(post("/api/wallets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
      "currency" : "USD"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode sgdWalletBody = objectMapper.readTree(sgdWalletResponse).path("data");
        JsonNode usdWalletBody = objectMapper.readTree(usdWalletResponse).path("data");
        UUID sgdWalletId = UUID.fromString(sgdWalletBody.path("id").asText());
        UUID usdWalletId = UUID.fromString(usdWalletBody.path("id").asText());

        Wallet sourceWallet = walletRepository.findById(sgdWalletId).orElseThrow();
        sourceWallet.setBalance(new BigDecimal("100.000000"));
        walletRepository.saveAndFlush(sourceWallet);

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
      "sourceWalletId" : "%s",
                           "targetWalletId" : "%s",
                                                "amount" : 25.000000,
                                                "idempotencyKey"
          : "idem-api-1",
            "note" : "portfolio demo"
                                }
                                """.formatted(sgdWalletId, usdWalletId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.sourceCurrency").value("SGD"))
                .andExpect(jsonPath("$.data.targetCurrency").value("USD"));

        mockMvc.perform(get("/api/wallets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/api/ledger/wallets/" + sgdWalletId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/ledger/wallets/" + usdWalletId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
  }
}
