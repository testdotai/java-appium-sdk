package ai.test.sdk;

import com.google.gson.JsonObject;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Actions;

/**
 * An enhanced RemoteWebElement which uses the results of the Test.ai classifier for improved accuracy.
 * 
 * @author Alexander Wu (alec@test.ai)
 *
 */
public class TestAiElement extends MobileElement
{
	/**
	 * The webdriver the user is using. We wrap this for when the user calls methods that interact with appium.
	 */
	@SuppressWarnings("rawtypes")
	private AppiumDriver driver;

	/**
	 * The text in this element, as determined by test.ai's classifier
	 */
	private String text;

	/**
	 * The size of this element, in pixels
	 */
	private Dimension size;

	/**
	 * The location of this element, in pixels (offset from the upper left corner of the screen)
	 */
	private Point location;

	/**
	 * The rectangle that can be drawn around this element. Basically combines size and location.
	 */
	private Rectangle rectangle;

	/**
	 * The tag name of this element, as determined by test.ai's classifier
	 */
	private String tagName;

	/**
	 * Coordinates for clicking/taping this element.
	 * 
	 * @see #click()
	 */
	private int cX, cY;

	/**
	 * Constructor, creates a new TestAiElement
	 * 
	 * @param elem The element data returned by the FD API, as JSON
	 * @param driver The driver the user is using to interact with their app
	 * @param multiplier The screen density multiplier to use
	 */
	TestAiElement(JsonObject elem, @SuppressWarnings("rawtypes") AppiumDriver driver, double multiplier)
	{
		this.driver = driver;

		text = JsonUtils.stringFromJson(elem, "text");
		size = new Dimension(JsonUtils.intFromJson(elem, "width") / (int) multiplier, JsonUtils.intFromJson(elem, "height") / (int) multiplier);

		location = new Point(JsonUtils.intFromJson(elem, "x") / (int) multiplier, JsonUtils.intFromJson(elem, "y") / (int) multiplier);

		// this.property = property //TODO: not referenced/implemented on python side??
		rectangle = new Rectangle(location, size);
		tagName = JsonUtils.stringFromJson(elem, "class");

		cX = location.x / (int) multiplier + size.width / (int) multiplier / 2;
		cY = location.y / (int) multiplier + size.height / (int) multiplier / 2;

	}

	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public Dimension getSize()
	{
		return size;
	}

	@Override
	public Point getLocation()
	{
		return location;
	}

	@Override
	public Rectangle getRect()
	{
		return rectangle;
	}

	@Override
	public String getTagName()
	{
		return tagName;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void click()
	{
		new TouchAction(driver).tap(PointOption.point(new Point(cX, cY))).perform();
	}

	@Override
	public void sendKeys(CharSequence... keysToSend)
	{
		sendKeys(String.join("", keysToSend), true);
	}

	/**
	 * Attempts to type the specified String ({@code value}) into this element.
	 * 
	 * @param value The String to type into this element.
	 * @param clickFirst Set {@code true} to tap this element (e.g. to focus it) first before sending keys.
	 */
	public void sendKeys(String value, boolean clickFirst)
	{
		if (clickFirst)
			click();

		new Actions(driver).sendKeys(value).perform();
	}

	@Override
	public void submit()
	{
		sendKeys("\n", false);
	}
}
