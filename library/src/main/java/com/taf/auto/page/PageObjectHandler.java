package com.taf.auto.page;

/**
 * This class is intended to replace the page objects that are stored in the step files.
 * This will allow the page objects to be installed at the first opportune time without
 * requiring extra work in each step.
 * Usage example:
 * <blockquote><pre>
 * public class FakePageSteps {
 *      PageObjectHandler&lt;FakePage&gt; fakePage = new PageObjectHandler&lt;&gt;(FakePage.class);
 *
 *      &amp;literal &amp;And("^Step Name$")
 *      public void stepName() {
 *          fakePage.get().doSomethingInPO();
 *      }
 * }
 * </pre></blockquote>
 */
public class PageObjectHandler<P extends AbstractPage> {
    private final Class<? extends P> clazz;
    private P page;

    public PageObjectHandler(Class <? extends P> clazz) {
        this.clazz = clazz;
    }

    public P get() {
        if (page == null) {
            page = AbstractPage.install(clazz);
        }
        return page;
    }
}
