package com.uzm.hylex.bedwars.controllers;

import com.uzm.hylex.bedwars.Core;
import com.uzm.hylex.core.java.util.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class DiscordController {


  private static LinkedHashMap<String, Long> COOLDOWN = new LinkedHashMap<>();

  public static void sendReport(String name, String command, String role, long currentMillis, String serverFullName) {

    if (COOLDOWN.containsKey(name)) {
      if (COOLDOWN.getOrDefault(name, 0L) >= System.currentTimeMillis()) {
       return;
      }else {
        COOLDOWN.remove(name);
      }
    }
    Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), () -> {
      try {

        StringEntity stringEntity = new StringEntity(
          "{\n" + "  \"embed\": {\n" + "      \"author\": {\n" + "        \"name\": \"AVISO PARA SUPERIORES\",\n" + "        \"icon_url\": \"https://i.imgur.com/zsoSpz8.png\"\n" + "      },\n" + "      \"title\": \"*Tentativa de Abuse do jogador " + name + "*\",\n" + "      \"description\": \"O staff *" + name + "* tentou executar comandos de moderação enquanto estava jogando!\",\n" + "      \"color\": 15158332,\n" + "      \"fields\": [\n" + "        {\n" + "          \"name\": \"Comando executado\",\n" + "          \"value\": \"" + command + "\",\n" + "          \"inline\": false\n" + "        },\n" + "        {\n" + "          \"name\": \"Servidor onde ele executou\",\n" + "          \"value\": \"" + serverFullName + "\",\n" + "          \"inline\": true\n" + "        },\n" + "\t\t\t\t {\n" + "          \"name\": \"Horário\",\n" + "          \"value\": \"" + StringUtils
            .formatDateBR(
              currentMillis) + "\",\n" + "          \"inline\": true\n" + "        },\n" + "\t\t\t\t  {\n" + "          \"name\": \"Cargo deste jogador\",\n" + "          \"value\": \"" + role + "\",\n" + "          \"inline\": false\n" + "        }\n" + "      ],\n" + "      \"thumbnail\": {\n" + "        \"url\": \"https://minotar.net/avatar/" + name + "\"\n" + "      },\n" + "      \"image\": {\n" + "        \"url\": \"https://cdn.psychologytoday.com/sites/default/files/styles/amp_metadata_content_image_min_1200px_wide/public/field_blog_entry_teaser_image/2018-03/punishment-144x144-192_0-664_664.jpg?itok=73q-94Wr\"\n" + "      },\n" + "      \"footer\": {\n" + "        \"text\": \"Equipe de Desenvolvimento RedeStone\",\n" + "        \"icon_url\": \"https://i.imgur.com/zsoSpz8.png\"\n" + "      }\n" + "    }\n" + "}",
          ContentType.APPLICATION_JSON);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("https://discordapp.com/api/channels/722636962128855101/messages");
        request.setHeader("Authorization", "Bot N/A");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(stringEntity);
        httpClient.execute(request);

        COOLDOWN.put(name, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));

      } catch (Exception e) {e.printStackTrace();}
    });
  }

}
