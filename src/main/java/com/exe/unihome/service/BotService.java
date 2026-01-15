package com.exe.unihome.service;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Stub bot service for generating AI responses.
 * This is a demo implementation that returns predefined responses.
 */
@Service
public class BotService {

  private static final String[] BOT_RESPONSES = {
    "That's an interesting question! I'll need more information to help you better.",
    "Thanks for reaching out. Let me think about that...",
    "I understand your concern. Here's what I suggest...",
    "That's a great point! Have you considered...",
    "I appreciate your feedback. Here are some options...",
    "Let me help you with that. The best approach would be...",
    "I see what you mean. Here's my recommendation...",
    "Thanks for the question! Based on your needs..."
  };

  private final Random random = new Random();

  /**
   * Generate a bot response to a user message.
   * This is a stub implementation that returns a random response.
   * In a real implementation, this would call an AI/ML service.
   */
  public String generateBotResponse(String userMessage) {
    // Stub: Return a random bot response
    String response = BOT_RESPONSES[random.nextInt(BOT_RESPONSES.length)];
    return response + " (Echo: " + userMessage.substring(0, Math.min(20, userMessage.length())) + ")";
  }
}
