package net.rejmi.pdfshow;

import com.darwinsys.swingui.RecentMenu;
import junit.framework.TestCase;
import org.junit.*;
import org.junit.runner.OrderWith;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

public class OpenCloseRecentTest {

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
            clearRecentItems();
            recentMenu.openFile(testFile.getAbsolutePath());
        } catch (IOException ex) {
            TestCase.fail();
        }
    }

    private void clearRecentItems() {
        for(Component component : applicationFrame.getJMenuBar().getComponents()) {
            if (((JMenu) component).getText().equalsIgnoreCase("file")) {
                for (int i = 0; i < ((JMenu) component).getItemCount(); i++) {
                    JMenuItem menuComponent = ((JMenu) component).getItem(i);
                    if (menuComponent != null && "clear recents list".equalsIgnoreCase(menuComponent.getText())) {
                        menuComponent.doClick();
                    }
                }
            }
        }
    }

    private Component findComponentInComponentTree(Container container, String componentName) {
        for(Component component : container.getComponents())
            if(componentName.equalsIgnoreCase(component.getName()))
                return component;

        return null;
    }

    private Component getPaginator() throws Exception {
        Component sideMenu = findComponentInComponentTree(applicationFrame.getContentPane(), "Panel_SideMenu");
        if(sideMenu != null) {
            Component navigation = findComponentInComponentTree((Container) sideMenu, "Panel_Navigation");
            if(navigation != null) {
                Component numbers = findComponentInComponentTree((Container) navigation, "Panel_Numbers");
                if(numbers != null) {
                    return numbers;
                }
            }
        }

        throw new Exception("Paginator not found");
    }

    @Test
    public void testPageNumbersStartsAt1AfterOpenFile() {
        openTestFile();

        try {
            JPanel paginator = (JPanel) getPaginator();
            JTextField actualPageComponent = (JTextField) findComponentInComponentTree(paginator, "TextField_ActualPage");

            Assert.assertNotNull(actualPageComponent);
            Assert.assertEquals(1, Integer.parseInt(actualPageComponent.getText()));
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testMaximumPageNumberIs5ForTestFile() {
        openTestFile();

        try {
            JPanel paginator = (JPanel) getPaginator();
            JLabel maxPageComponent = (JLabel) findComponentInComponentTree(paginator, "Label_TotalPages");

            Assert.assertNotNull(maxPageComponent);
            Assert.assertEquals(5, Integer.parseInt(maxPageComponent.getText()));
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCanMoveThroughAllPagesWithNavigationButton() {
        openTestFile();

        try {
            JButton buttonDown = null;
            Component sideMenu = findComponentInComponentTree(applicationFrame.getContentPane(), "Panel_SideMenu");
            if(sideMenu != null) {
                Component navigation = findComponentInComponentTree((Container) sideMenu, "Panel_Navigation");
                if (navigation != null) {
                    buttonDown = (JButton) findComponentInComponentTree((Container) navigation, "NavigationButton_Down");
                }
            }

            if(buttonDown != null) {
                JPanel paginator = (JPanel) getPaginator();
                JTextField actualPage = (JTextField) findComponentInComponentTree(paginator, "TextField_ActualPage");
                JLabel maxPagesComponent = (JLabel) findComponentInComponentTree(paginator, "Label_TotalPages");
                int maxPages = Integer.parseInt(maxPagesComponent.getText());

                for(int page = 1; page <= maxPages; page++) {
                    Assert.assertEquals(page, Integer.parseInt(actualPage.getText()));
                    buttonDown.doClick();
                }

            } else {
                Assert.fail("Couln't find test button");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRecentOpenedItemsAfterReopeningApp() {
        setup();

        for(Component component : applicationFrame.getJMenuBar().getComponents()) {
            if (((JMenu) component).getText().equalsIgnoreCase("file")) {
                for (int i = 0; i < ((JMenu) component).getItemCount(); i++) {
                    JMenuItem menuComponent = ((JMenu) component).getItem(i);

                    if(menuComponent != null && "recent items".equalsIgnoreCase(menuComponent.getText())) {
                        JPopupMenu recentPopUp = ((RecentMenu) menuComponent).getPopupMenu();
                        for(Component recentItem : recentPopUp.getComponents()) {
                            System.out.println((((JMenuItem) recentItem).getText()));
                            Assert.assertTrue((((JMenuItem) recentItem).getText()).contains("SampleShow.pdf"));
                        }
                    }
                }
            }
        }
    }
}
