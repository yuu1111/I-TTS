package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * サーバー辞書
 *
 * @author MORIMORI0317
 */
public class ServerDictionary extends CustomDictBaseDictionary {

    @Override
    public @NotNull String getName() {
        return "サーバー辞書";
    }

    @Override
    public @NotNull String getId() {
        return "server";
    }

    @Override
    public int getDefaultPriority() {
        return 2;
    }

    @Override
    protected @NotNull List<LegacyDictData> fetchData(long guildId) {
        return SaveDataManager.getInstance().getLegacySaveDataLayer().getAllServerDictData(guildId);
    }

    @Override
    protected @NotNull String getDictLogName() {
        return "server";
    }
}
