package de.jeff_media.AngelChest;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class GroupUtils {

    Main main;
    YamlConfiguration yaml;
    LinkedHashMap<String,Group> groups;
    GroupUtils(Main main, File yamlFile) {
        this.main=main;
        if(!yamlFile.exists()) {
            main.getLogger().info("groups.yml does not exist, skipping custom group settings.");
            return;
        }
        this.yaml=YamlConfiguration.loadConfiguration(yamlFile);
        groups = new LinkedHashMap<String,Group>();

        for(String groupName : yaml.getKeys(false)) {
            int angelchestDuration = yaml.getInt(groupName+".angelchest-duration",-1);
            //System.out.println("Registering group "+groupName);
            groups.put(groupName, new Group(angelchestDuration));
        }
    }

    int getDurationPerPlayer(Player p) {
        if(yaml==null) return main.getConfig().getInt("angelchest-duration");
        Iterator<String> it = groups.keySet().iterator();
        int bestValueFound = -1;
        while(it.hasNext()) {
            String group = it.next();
            if(!p.hasPermission("angelchest.group."+group)) continue;
            //System.out.println(" Player is in group "+group);
            int angelchestDuration = groups.get(group).angelchestDuration;
            bestValueFound = (angelchestDuration>bestValueFound) ? angelchestDuration : bestValueFound;
            //System.out.println("best value found: "+bestValueFound);
        }
        return bestValueFound == -1 ? main.getConfig().getInt("angelchest-duration") : bestValueFound;
    }

    class Group {
        int angelchestDuration;

        Group(int angelchestDuration) {
            this.angelchestDuration = angelchestDuration;
        }
    }

}
