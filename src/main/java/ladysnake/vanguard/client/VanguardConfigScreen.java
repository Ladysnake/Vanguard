package ladysnake.vanguard.client;

import io.github.prospector.modmenu.gui.ModListWidget;
import ladysnake.vanguard.common.Config;
import ladysnake.vanguard.common.Vanguard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.client.options.BooleanOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class VanguardConfigScreen extends Screen {
    private final Screen parent;
    private ModListWidget modList;
    private int paneY;
    private int paneWidth;
    private int rightPaneX;

    private BooleanOption TOGGLE = new BooleanOption("options.vanguard.toggle", (gameOptions) -> {
        return Config.isToggled();
    }, (gameOptions, aBoolean) -> {
        Config.toggle(aBoolean);
    });

    public VanguardConfigScreen(Screen parent) {
        super(new TranslatableText("title.vanguard.config"));
        this.parent = parent;
    }

    protected void init() {
        int i = 0;
        paneY = 48;
        paneWidth = this.width / 2 - 8;
        rightPaneX = width - paneWidth;


        MutableText toggleText;
        if (Config.isToggled()) {
            toggleText = new TranslatableText("option.vanguard.toggle").append(new TranslatableText("toggle."+Config.isToggled()).formatted(Formatting.GREEN));
        } else {
            toggleText = new TranslatableText("option.vanguard.toggle").append(new TranslatableText("toggle."+Config.isToggled()).formatted(Formatting.RED));
        }

        this.addButton(new OptionButtonWidget(this.width / 2 - 100, this.height / 6 + 48 - 6, 200, 20, TOGGLE, toggleText, (button) -> {
            Config.toggle(!Config.isToggled());
            if (Config.isToggled()) {
                button.setMessage(new TranslatableText("option.vanguard.toggle").append(new TranslatableText("toggle."+Config.isToggled()).formatted(Formatting.GREEN)));
            } else {
                button.setMessage(new TranslatableText("option.vanguard.toggle").append(new TranslatableText("toggle."+Config.isToggled()).formatted(Formatting.RED)));
            }
        }));

        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.DONE, (button) -> {
            this.client.openScreen(this.parent);
        }));
    }

    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (modList.isMouseOver(double_1, double_2)) {
            return this.modList.mouseScrolled(double_1, double_2, double_3);
        }
        return false;
    }

    public void removed() {
        Config.save();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        for (int i = 0; i < Vanguard.getMods().size(); i += 5) {
            drawCenteredText(matrices, this.textRenderer, createModsText(i, i+4), this.width / 2, 120+(10*i/5), 16777215);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    public static LiteralText createModsText(int startI, int endI) {
        LiteralText ret = new LiteralText("");
        for (int i = startI; i <= endI; i++) {
            if (i < Vanguard.getMods().size()) {
                String mod = Vanguard.getMods().get(i);
                LiteralText modText = new LiteralText(FabricLoader.getInstance().getModContainer(mod).get().getMetadata().getName());
                if (Vanguard.getUpdatedMods().contains(mod)) {
                    modText.formatted(Formatting.YELLOW);
                }
                if (i < Vanguard.getMods().size()-1) {
                    modText.append(", ");
                }
                ret.append(modText);
            }
        }

        return ret;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.client.openScreen(this.parent);
    }

}
