package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * グローバル辞書
 *
 * @author MORIMORI0317
 */
public class GlobalDictionary extends CustomDictBaseDictionary {

    @Override
    public @NotNull String getName() {
        return "グローバル辞書";
    }

    @Override
    public @NotNull String getId() {
        return "global";
    }

    @Override
    public int getDefaultPriority() {
        return 3;
    }

    @Override
    protected @NotNull List<LegacyDictData> fetchData(long guildId) {
        return SaveDataManager.getInstance().getLegacySaveDataLayer().getAllGlobalDictData();
    }

    @Override
    protected @NotNull String getDictLogName() {
        return "global";
    }
}
