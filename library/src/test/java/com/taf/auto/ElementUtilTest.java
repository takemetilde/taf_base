package com.taf.auto;

import com.taf.auto.ElementUtil;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import static com.taf.auto.ElementUtil.webElementByHelper;
import static org.junit.Assert.*;

/**
 * Test cases for {@link ElementUtil}.
 *
 * @author mmorton
 */
public class ElementUtilTest {
    @Test
    public void webElementToString() {
        String to = "[[ChromeDriver: chrome on XP (90e2b19c4f175b8afeaff604c51080a5)] -> id: txtUsername]";
        assertEquals("[id: txtUsername]", webElementByHelper(to));

        to = "blah";
        assertEquals(to, webElementByHelper(to));
    }

    @Test(expected = RuntimeException.class)
    public void getByFromField_noField() {
        ElementUtil.getByFromField(FindByTestClass.class, "noField");
    }

    @Test(expected = RuntimeException.class)
    public void getByFromField_noFindBy() {
        ElementUtil.getByFromField(FindByTestClass.class, "noFindBy");
    }

    @Test
    public void getByFromField_withFindBy() {
        By byMissAmericanPie = ElementUtil.getByFromField(FindByTestClass.class, "withFindBy");
        assertNotNull(byMissAmericanPie);
    }

    @Test
    public void getByFromField_withFindBys() {
        By byMissAmericanPie = ElementUtil.getByFromField(FindByTestClass.class, "withFindBys");
        assertNotNull(byMissAmericanPie);
    }

    @Test
    public void getByFromField_withFindAll() {
        By byMissAmericanPie = ElementUtil.getByFromField(FindByTestClass.class, "withFindAll");
        assertNotNull(byMissAmericanPie);
    }

    private class FindByTestClass {
        private WebElement noFindBy;

        @FindBy(id = "test")
        private WebElement withFindBy;

        @FindBys({
            @FindBy(id = "test"),
            @FindBy(name = "test2")
        })
        private WebElement withFindBys;

        @FindAll({
            @FindBy(id = "test"),
            @FindBy(name = "test2")
        })
        private WebElement withFindAll;
    }
}
