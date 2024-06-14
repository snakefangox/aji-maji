package xyz.fancyteam.ajimaji.top_hat;

import net.minecraft.entity.Entity;

public interface TopHatRetrieveListener {
    void acceptRetrieved(Entity retrieved);

    void notifyMissing();
}
