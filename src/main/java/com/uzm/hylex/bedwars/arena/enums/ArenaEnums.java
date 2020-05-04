package com.uzm.hylex.bedwars.arena.enums;

import java.util.Arrays;
import java.util.List;

public class ArenaEnums {

  /*
   6 minutos
   6 minutos
   6 minutos
   6 minutos
   6 minutos
   6 minutos
   */
  public enum Events {
    IDLE("", "", 0, 0, 0, 0),
    DIAMOND_II("Diamante II", "", 1, 1, 65, 30),
    EMERALD_II("Esmeralda II", "§eOs geradores de §bDiamante §eforam evoluídos para o nível §cII§e.", 2, 1, 65, 23),
    DIAMOND_III("Diamante III", "§eOs geradores de §aEsmeralda §eforam evoluídos para o nível §cII§e.", 2, 2, 50, 23),
    EMERALD_III("Esmeralda III", "§eOs geradores de §bDiamante §eforam evoluídos para o nível §cIII§e.", 3, 2, 50, 15),
    BED_DESTRUCTION("Remover camas", "§eOs geradores de §aEsmeralda §eforam evoluídos para o nível §cIII§e.", "§cAs camas serão destruidas em %s %format.", 3, 3, 35, 15),
    SUDDEN_DEATH("Morte súbita", "§cTodas as camas de todos os times foram §cdestruídas§c.", "§cMorte súbita em %s %format.", 3, 3, 35, 15),
    GAME_END("Fim de jogo", "§c§lMORTE SÚBITA.", "§cO jogo terminará automaticamente em %s %format.", 3, 3, 35, 15);

    private String name;
    private String message;
    private String subMessage;
    private int emeraldLevel;
    private int diamondLevel;

    private int emeraldDelay;
    private int diamondDelay;


    Events(String name, String message, int diamondLevel, int emeraldLevel, int emeraldDelay, int diamondDelay) {
      this.name = name;
      this.diamondLevel = diamondLevel;
      this.emeraldLevel = emeraldLevel;
      this.emeraldDelay = emeraldDelay;
      this.diamondDelay = diamondDelay;
      this.message = message;
    }

    Events(String name, String message, String subMessage, int diamondLevel, int emeraldLevel, int emeraldDelay, int diamondDelay) {
      this.name = name;
      this.diamondLevel = diamondLevel;
      this.emeraldLevel = emeraldLevel;
      this.emeraldDelay = emeraldDelay;
      this.diamondDelay = diamondDelay;
      this.message = message;
      this.subMessage = subMessage;
    }

    public String getName() {
      return name;
    }

    public String getMessage() {
      return message;
    }

    public String getSubMessage() {
      return subMessage;
    }

    public int getEmeraldLevel() {
      return emeraldLevel;
    }

    public int getDiamondLevel() {
      return diamondLevel;
    }

    public int getDiamondDelay() {
      return diamondDelay;
    }

    public int getEmeraldDelay() {
      return emeraldDelay;
    }

    private static final List<Events> EVENTS;

    static {
      EVENTS = Arrays.asList(values());
    }

    public Events next() {
      return (EVENTS.indexOf(this) + 1) == EVENTS.size() ? GAME_END : EVENTS.get((EVENTS.indexOf(this) + 1));
    }
  }
}
