package com.doodlechaos.playersync;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MyListScreen extends Screen {
    private final MinecraftClient client;
    private final List<String> items; // Our list of strings
    private MyListWidget listWidget;  // Our custom list widget
    private ButtonWidget addButton;
    private ButtonWidget doneButton;

    //private Element focusedElement; // Track our focused element

    public MyListScreen(MinecraftClient client, List<String> initialItems) {
        super(Text.of("Custom List Screen"));
        this.client = client;
        this.items = initialItems;
    }

    @Override
    protected void init() {
        super.init();

        // Create our custom list widget.
        // Parameters: client, width, height, top, bottom, itemHeight.
        this.listWidget = new MyListWidget(this.client, this.width, this.height, 40, this.height - 40, 24);
        // Add existing items to the widget.
        for (String s : items) {
            this.listWidget.addCustomEntry(this.listWidget.new MyListWidgetEntry(s));
        }
        this.addSelectableChild(this.listWidget);

        // "Add" button creates a new entry with default text.
        this.addButton = ButtonWidget.builder(Text.of("Add"), button -> {
            this.listWidget.addCustomEntry(this.listWidget.new MyListWidgetEntry("New Entry"));
        }).dimensions(this.width / 2 - 50, this.height - 28, 40, 20).build();
        this.addDrawableChild(this.addButton);

        // "Done" button gathers the list strings and closes the screen.
        this.doneButton = ButtonWidget.builder(Text.of("Done"), button -> {
            this.items.clear();
            this.items.addAll(this.listWidget.getStrings());
            this.close();
            PlayerSync.OpenScreen = false;
        }).dimensions(this.width / 2 + 10, this.height - 28, 40, 20).build();
        this.addDrawableChild(this.doneButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.listWidget.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        int textWidth = this.textRenderer.getWidth(this.title.getString());
        context.drawText(this.textRenderer, this.title.getString(), (this.width - textWidth) / 2, 10, 0xFFFFFF, true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        // Update the items list on close.
        this.items.clear();
        this.items.addAll(this.listWidget.getStrings());
        super.close();
    }


    ///////////////////////////////////////////////////////////////////////////////
    // Custom List Widget Class (extends EntryListWidget) with a public wrapper.
    ///////////////////////////////////////////////////////////////////////////////
    public class MyListWidget extends EntryListWidget<MyListWidget.MyListWidgetEntry> {

        public MyListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);
            this.setRenderBackground(true);
        }

        // Public wrapper for the protected addEntry(...) method.
        public int addCustomEntry(MyListWidgetEntry entry) {
            return super.addEntry(entry);
        }

        // Returns the current list of strings from all entries.
        public List<String> getStrings() {
            List<String> result = new ArrayList<>();
            for (MyListWidgetEntry entry : this.children()) {
                result.add(entry.getText());
            }
            return result;
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {

        }

        ///////////////////////////////////////////////////////////////////////////////
        // Custom Entry Class (each row in the list)
        // Note: This class is now inside MyListWidget, so it has access to the
        // protected inner class EntryListWidget.Entry.
        ///////////////////////////////////////////////////////////////////////////////
        public class MyListWidgetEntry extends EntryListWidget.Entry<MyListWidgetEntry> {
            private TextFieldWidget textField;
            private ButtonWidget removeButton;
            private ButtonWidget upButton;
            private ButtonWidget downButton;
            private final int ENTRY_HEIGHT = 24;
            private String initialText;

            public MyListWidgetEntry(String initialText) {
                this.initialText = initialText;
            }
            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
                final int numberWidth = 20; // Space reserved for the entry number
                int offsetX = x + numberWidth;

                // Draw the entry number (1-based) on the far left.
                int number = index + 1;
                int textHeight = client.textRenderer.fontHeight;
                int numberX = x + 2; // small left padding
                int numberY = y + (ENTRY_HEIGHT - textHeight) / 2;
                context.drawText(client.textRenderer, Text.of(String.valueOf(number)), numberX, numberY, 0xFFFFFF, false);

                // 1) removeButton now placed at offsetX + 5
                if (this.removeButton == null) {
                    this.removeButton = ButtonWidget.builder(Text.of("X"), btn -> {
                        MyListWidget.this.children().remove(this);
                    }).dimensions(offsetX + 5, y, 20, 20).build();
                } else {
                    this.removeButton.setX(offsetX + 5);
                    this.removeButton.setY(y);
                }

                // 2) upButton, shifted similarly.
                if (this.upButton == null) {
                    this.upButton = ButtonWidget.builder(Text.of("^"), btn -> moveUp())
                            .dimensions(offsetX + 30, y, 20, 20)
                            .build();
                } else {
                    this.upButton.setX(offsetX + 30);
                    this.upButton.setY(y);
                }

                // 3) downButton, shifted similarly.
                if (this.downButton == null) {
                    this.downButton = ButtonWidget.builder(Text.of("v"), btn -> moveDown())
                            .dimensions(offsetX + 55, y, 20, 20)
                            .build();
                } else {
                    this.downButton.setX(offsetX + 55);
                    this.downButton.setY(y);
                }

                // 4) The text field now starts after the buttons.
                if (this.textField == null) {
                    int textFieldX = offsetX + 80;
                    int textFieldY = y + (ENTRY_HEIGHT - 16) / 2;
                    int textFieldWidth = entryWidth - numberWidth - 85;  // reduce width to account for the reserved number space
                    this.textField = new TextFieldWidget(client.textRenderer, textFieldX, textFieldY, textFieldWidth, 16, Text.of("entry"));
                    this.textField.setMaxLength(10000);
                    this.textField.setText(this.initialText);
                } else {
                    this.textField.setX(offsetX + 80);
                    this.textField.setY(y + (ENTRY_HEIGHT - 16) / 2);
                    this.textField.setWidth(entryWidth - numberWidth - 85);
                }

                // Render all elements:
                this.textField.render(context, mouseX, mouseY, delta);
                this.removeButton.render(context, mouseX, mouseY, delta);
                this.upButton.render(context, mouseX, mouseY, delta);
                this.downButton.render(context, mouseX, mouseY, delta);
            }


            private void moveUp() {
                int currentIndex = MyListWidget.this.children().indexOf(this);
                if (currentIndex > 0) {
                    List<MyListWidgetEntry> entries = MyListWidget.this.children();
                    MyListWidgetEntry other = entries.get(currentIndex - 1);
                    entries.set(currentIndex - 1, this);
                    entries.set(currentIndex, other);
                }
            }

            private void moveDown() {
                int currentIndex = MyListWidget.this.children().indexOf(this);
                if (currentIndex < MyListWidget.this.children().size() - 1) {
                    List<MyListWidgetEntry> entries = MyListWidget.this.children();
                    MyListWidgetEntry other = entries.get(currentIndex + 1);
                    entries.set(currentIndex + 1, this);
                    entries.set(currentIndex, other);
                }
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (textField == null) {
                    PlayerSync.LOGGER.error("Missing textField");
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
                textField.setEditable(true);
                textField.setFocusUnlocked(true);
                textField.setFocused(true);

                if (textField.keyPressed(keyCode, scanCode, modifiers)) {
                    PlayerSync.LOGGER.info("Detected key pressed in textField");
                    return true;
                }
                PlayerSync.LOGGER.info("Key pressed passed to super");
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean charTyped(char chr, int modifiers) {
                if (textField == null) {
                    PlayerSync.LOGGER.error("Missing textField");
                    return super.charTyped(chr, modifiers);
                }
                textField.setEditable(true);
                textField.setFocusUnlocked(true);
                textField.setFocused(true);

                if (textField.charTyped(chr, modifiers)) {
                    PlayerSync.LOGGER.info("Detected char typed in textField");
                    return true;
                }
                PlayerSync.LOGGER.info("Char typed passed to super");
                return super.charTyped(chr, modifiers);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.textField != null && this.textField.mouseClicked(mouseX, mouseY, button)) {
                    // Tell the screen which element should now be focused.
                    MyListScreen.this.setFocused(this.textField);
                    this.textField.setFocused(true);
                    this.textField.setEditable(true);
                    return true;
                }
                // Handle button clicks as before.
                if (this.removeButton != null && this.removeButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                if (this.upButton != null && this.upButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                if (this.downButton != null && this.downButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                return false;
            }

            public String getText() {
                return (this.textField == null) ? this.initialText : this.textField.getText();
            }
        }
        @Override
        public int getRowWidth() {
            // This will use almost the full screen width minus a bit of padding.
            return MyListScreen.this.width - 20;
        }
        @Override
        protected int getScrollbarPositionX() {
            // Place the scrollbar just to the right of the entry edge.
            return MyListScreen.this.width - 10;
        }

    }
}
