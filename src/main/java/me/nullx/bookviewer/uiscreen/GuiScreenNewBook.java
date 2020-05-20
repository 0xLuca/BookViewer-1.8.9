package me.nullx.bookviewer.uiscreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuiScreenNewBook extends GuiScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation BOOK_GUI_TEXTURES = new ResourceLocation("textures/gui/book.png");


    /**
     * Whether the book is signed or can still be edited
     */
    private final boolean bookIsUnsigned;

    private int bookTotalPages = 1;
    private int currPage;
    private List<String> pages;
    private List<ChatComponentText> cachedComponents;
    private GuiScreenNewBook.NextPageButton buttonNextPage;
    private GuiScreenNewBook.NextPageButton buttonPreviousPage;
    private GuiButton buttonDone;

    public GuiScreenNewBook(String... pages) {
        this.bookIsUnsigned = false;

        this.pages = Arrays.asList(pages);
        this.bookTotalPages = this.pages.size();

        if (this.bookTotalPages < 1) {
            this.bookTotalPages = 1;
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        super.updateScreen();
    }

    private <T extends GuiButton> T addButton(T button) {
        if (button != null) {
            this.buttonList.add(button);
        }
        return button;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        this.buttonDone = this.addButton(new GuiButton(0, this.width / 2 - 100, 196, 200, 20, I18n.format("gui.done")));

        int i = (this.width - 192) / 2;
        this.buttonNextPage = this.addButton(new GuiScreenNewBook.NextPageButton(1, i + 120, 156, true));
        this.buttonPreviousPage = this.addButton(new GuiScreenNewBook.NextPageButton(2, i + 38, 156, false));
        this.updateButtons();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void updateButtons() {
        this.buttonNextPage.visible = (this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned);
        this.buttonPreviousPage.visible = this.currPage > 0;
        this.buttonDone.visible = !this.bookIsUnsigned;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 0) {
                mc.displayGuiScreen(null);
            } else if (button.id == 1) {
                if (this.currPage < this.bookTotalPages - 1) {
                    ++this.currPage;
                }
            } else if (button.id == 2) {
                if (this.currPage > 0) {
                    --this.currPage;
                }
            }

            this.updateButtons();
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        try {
            this.mc.getTextureManager().bindTexture(BOOK_GUI_TEXTURES);
        } catch (Throwable t) { return; }
        int i = (this.width - 192) / 2;
        int j = 2;
        this.drawTexturedModalRect(i, 2, 0, 0, 192, 192);


        try {
            Mouse.setNativeCursor(Mouse.getNativeCursor());
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        String s4 = I18n.format("book.pageIndicator", this.currPage + 1, this.bookTotalPages);
        String s5 = "";

        if (this.currPage >= 0 && this.currPage < this.pages.size()) {
            s5 = this.pages.get(this.currPage);
        }

        int j1 = this.fontRendererObj.getStringWidth(s4);
        this.fontRendererObj.drawString(s4, i - j1 + 192 - 44, 18, 0);

        if (this.cachedComponents == null) {
            this.fontRendererObj.drawSplitString(s5, i + 36, 34, 116, 0);
        } else {
            int k1 = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.cachedComponents.size());

            for (int l1 = 0; l1 < k1; ++l1) {
                ChatComponentText itextcomponent2 = this.cachedComponents.get(l1);
                this.fontRendererObj.drawString(itextcomponent2.getUnformattedText(), i + 36, 34 + l1 * this.fontRendererObj.FONT_HEIGHT, 0);
            }

            ChatComponentText itextcomponent1 = this.getClickedComponentAt(mouseX, mouseY);

            if (itextcomponent1 != null) {
                this.handleComponentHover(itextcomponent1, mouseX, mouseY);
            }
        }


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            ChatComponentText itextcomponent = this.getClickedComponentAt(mouseX, mouseY);

            if (itextcomponent != null && this.handleComponentClick(itextcomponent)) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Executes the click event specified by the given chat component
     */
    public boolean handleComponentClick(ChatComponentText component) {
        ClickEvent clickevent = component.getChatStyle().getChatClickEvent();

        if (clickevent == null) {
            return false;
        } else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String s = clickevent.getValue();

            try {
                int i = Integer.parseInt(s) - 1;

                if (i >= 0 && i < this.bookTotalPages && i != this.currPage) {
                    this.currPage = i;
                    this.updateButtons();
                    return true;
                }
            } catch (Throwable var5) {
                ;
            }

            return false;
        } else {
            boolean flag = super.handleComponentClick(component);

            if (flag && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.mc.displayGuiScreen((GuiScreen) null);
            }

            return flag;
        }
    }

    @Nullable
    public ChatComponentText getClickedComponentAt(int p_175385_1_, int p_175385_2_) {
        if (this.cachedComponents == null) {
            return null;
        } else {
            int i = p_175385_1_ - (this.width - 192) / 2 - 36;
            int j = p_175385_2_ - 2 - 16 - 16;

            if (i >= 0 && j >= 0) {
                int k = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.cachedComponents.size());

                if (i <= 116 && j < this.mc.fontRendererObj.FONT_HEIGHT * k + k) {
                    int l = j / this.mc.fontRendererObj.FONT_HEIGHT;

                    if (l >= 0 && l < this.cachedComponents.size()) {
                        ChatComponentText itextcomponent = this.cachedComponents.get(l);
                        int i1 = 0;

                        for (IChatComponent itextcomponent1 : itextcomponent) {
                            if (itextcomponent1 instanceof ChatComponentText) {
                                i1 += this.mc.fontRendererObj.getStringWidth(((ChatComponentText) itextcomponent1).getUnformattedText());

                                if (i1 > i) {
                                    return (ChatComponentText) itextcomponent1;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    static class NextPageButton extends GuiButton {
        private final boolean isForward;

        public NextPageButton(int p_i46316_1_, int p_i46316_2_, int p_i46316_3_, boolean p_i46316_4_) {
            super(p_i46316_1_, p_i46316_2_, p_i46316_3_, 23, 13, "");
            this.isForward = p_i46316_4_;
        }

        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(GuiScreenNewBook.BOOK_GUI_TEXTURES);
                int i = 0;
                int j = 192;

                if (flag)
                {
                    i += 23;
                }

                if (!this.isForward)
                {
                    j += 13;
                }

                this.drawTexturedModalRect(this.xPosition, this.yPosition, i, j, 23, 13);
            }
        }
    }
}
