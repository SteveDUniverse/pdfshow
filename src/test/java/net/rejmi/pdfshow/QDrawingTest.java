package net.rejmi.pdfshow;

import com.darwinsys.swingui.RecentMenu;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class QDrawingTest {

    private static JFrame applicationFrame;
    private static PdfShow applicationInstance;

    private static RecentMenu recentMenu;
    private static File testFile;

    @BeforeClass
    public static void setup() {
        String args[] = {};
        try {
            PdfShow.main(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            TestCase.fail("Error while trying start application");
        }

        applicationFrame = PdfShow.frame;
        applicationInstance = PdfShow.instance;
        recentMenu = applicationInstance.recents;
        testFile = new File("samples/SampleShow.pdf");
    }

    private void openTestFile() {
        try {
            recentMenu.openFile(testFile.getAbsolutePath());
        } catch (IOException ex) {
            TestCase.fail();
        }
    }

    private Component findComponentInComponentTree(Container container, String componentName) throws Exception {
        Component component = null;

        for(Component insideComponent : container.getComponents()) {
            if (componentName.equalsIgnoreCase(insideComponent.getName())) {
                component = insideComponent;
                break;
            }
        }

        if(component == null)
            throw new Exception("Component not found");

        return component;
    }

    public List<Component> getAllComponents(Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container)
                compList.addAll(getAllComponents((Container) comp));
        }
        return compList;
    }

    @Test
    public void testAddTextToPageAndCheckColorAndFont() {
        openTestFile();

        try {
            JPanel sideMenu = (JPanel) findComponentInComponentTree(applicationFrame.getContentPane(), "Panel_SideMenu");
            JPanel toolBox = (JPanel) findComponentInComponentTree(sideMenu, "Panel_ToolBox");
            JButton addTextButton = (JButton) findComponentInComponentTree(toolBox, "Button_Text");

            addTextButton.doClick();

//            MouseEvent mousePressed = new MouseEvent(applicationFrame.getContentPane(), MouseEvent.MOUSE_PRESSED, 1000, InputEvent.BUTTON1_DOWN_MASK, 500, 180, 500, 180, 1, true, MouseEvent.BUTTON1);
//            applicationFrame.dispatchEvent(mousePressed);
//            Events wont do because program is waiting to void response to dispatchEvent function, I think

            Robot bot = new Robot();
            bot.mouseMove(500,180);
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(250);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            List<Component> dialogComponents = new LinkedList<>();
            for(Window dialog : JDialog.getWindows())
                if("Dialog_TextInput".equalsIgnoreCase(dialog.getName()))
                    dialogComponents.addAll(getAllComponents(dialog));

            List<Component> dialogTextInputComponent = new LinkedList<>();
            for(Component dialogComponent : dialogComponents)
                if("Panel_TextInput".equalsIgnoreCase(dialogComponent.getName()))
                    dialogTextInputComponent.addAll(getAllComponents((Container) dialogComponent));

            JTextField textInput = null;
            for(Component textInputPanelComponent : dialogTextInputComponent)
                if(textInputPanelComponent instanceof JTextField)
                    textInput = (JTextField) textInputPanelComponent;

            if(textInput == null)
                throw new Exception("Couldn't find text input");

            textInput.setText("Hello world.");
            bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(250);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            applicationInstance.visitCurrentPageGObjs(gObject -> {
                if(gObject instanceof GText) {
                    GText text = (GText) gObject;

                    Assert.assertEquals(text.color.getRed(), 255);
                    Assert.assertEquals(text.color.getGreen(), 0);
                    Assert.assertEquals(text.color.getBlue(), 0);
                    Assert.assertEquals("Sans", text.font.getName());
                }
            });
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }
}
