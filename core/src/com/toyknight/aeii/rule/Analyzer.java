package com.toyknight.aeii.rule;

import static com.toyknight.aeii.rule.Rule.Entry.*;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Tile;

/**
 * @author toyknight 1/10/2016.
 */
public class Analyzer {

    private final GameCore game;

    public Analyzer(GameCore game) {
        this.game = game;
    }

    public GameCore getGame() {
        return game;
    }

    public boolean isTeamDestroyed(int team) {
        boolean unit_check = true;
        if (getGame().getRule().getBoolean(ENEMY_CLEAR)) {
            for (Point position : getGame().getMap().getUnitPositionSet()) {
                if (getGame().getMap().getUnit(position).getTeam() == team) {
                    unit_check = false;
                    break;
                }
            }
        }
        boolean castle_check = true;
        if (getGame().getRule().getBoolean(CASTLE_CLEAR)) {
            for (int x = 0; x < getGame().getMap().getWidth(); x++) {
                for (int y = 0; y < getGame().getMap().getHeight(); y++) {
                    Tile tile = getGame().getMap().getTile(x, y);
                    if (tile.isCastle() && tile.getTeam() == team) {
                        castle_check = false;
                        break;
                    }
                }
            }
        }
        return unit_check && castle_check;
    }

    public int getWinnerAlliance() {
        int alliance = -1;
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            if (alliance == -1) {
                if (player.getType() != Player.NONE) {
                    alliance = player.getAlliance();
                }
            } else {
                if (player.getType() != Player.NONE && player.getAlliance() != alliance) {
                    alliance = -1;
                    break;
                }
            }
        }
        return alliance;
    }

}
