package ai.test.sdk;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.DeviceRotation;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.ErrorHandler;
import org.openqa.selenium.remote.ExecuteMethod;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * A convenient wrapper around {@code AppiumDriver} which calls out to Test.ai to improve the accuracy of identified elements.
 * 
 * @author Alexander Wu (alec@test.ai)
 *
 * @param <T> The element type to return, must be a subclass of MobileElement.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class TestAiDriver<T extends WebElement> // extends AppiumDriver<T>
{
	/**
	 * The logger for this class
	 */
	private static Logger log = LoggerFactory.getLogger(TestAiDriver.class);

	/**
	 * The client to use for making http requests
	 */
	private OkHttpClient client;

	/**
	 * The driver used by the user that we're wrapping.
	 */
	private AppiumDriver<T> driver;

	/**
	 * The user's fluffy dragon API key
	 */
	private String apiKey;

	/**
	 * The base URL of the target server (e.g. {@code https://sdk.test.ai})
	 */
	private HttpUrl serverURL;

	/**
	 * The test case name. Used in live/interactive mode.
	 */
	private String testCaseName;

	/**
	 * Indicates whether Test.ai should be used to improve the accuracy of returned elements
	 */
	// private boolean train;

	/**
	 * The run id. This should be randomly generated each run.
	 */
	private String runID = UUID.randomUUID().toString();

	/**
	 * The UUID of the last screenshot in live/interactive mode.
	 */
	// private String lastTestCaseScreenshotUUID;

	/**
	 * The screen density multiplier
	 */
	private double multiplier;

	/**
	 * Constructor, creates a new TestAiDriver.
	 * 
	 * @param driver The AppiumDriver to wrap
	 * @param apiKey Your API key, acquired from <a href="https://sdk.test.ai">sdk.test.ai</a>.
	 * @param serverURL The server URL. Set {@code null} to use the default of <a href="https://sdk.test.ai">sdk.test.ai</a>.
	 * @param testCaseName The test case name to use for interactive mode. Setting this to something other than {@code null} enables interactive mode.
	 * @param train Set `true` to enable training for each encountered element.
	 * @throws IOException If there was an initialization error.
	 */
	public TestAiDriver(AppiumDriver<T> driver, String apiKey, String serverURL, String testCaseName, boolean train) throws IOException
	{
		// super(driver.getCapabilities());

		this.driver = driver;
		this.apiKey = apiKey;
		this.testCaseName = testCaseName;
		// this.train = train;

		this.serverURL = HttpUrl.parse(serverURL != null ? serverURL : Objects.requireNonNullElse(System.getenv("TESTAI_FLUFFY_DRAGON_URL"), "https://sdk.test.ai"));
		client = this.serverURL.equals(HttpUrl.parse("https://sdk.dev.test.ai")) ? NetUtils.unsafeClient() : NetUtils.basicClient().build();

		multiplier = 1.0 * ImageIO.read(driver.getScreenshotAs(OutputType.FILE)).getWidth() / driver.manage().window().getSize().width;

		log.debug("The screen multiplier is {}", multiplier);
	}

	/**
	 * Constructor, creates a new TestAiDriver with the default server url (<a href="https://sdk.test.ai">sdk.test.ai</a>), non-interactive mode, and with training enabled.
	 * 
	 * @param driver The AppiumDriver to wrap
	 * @param apiKey Your API key, acquired from <a href="https://sdk.test.ai">sdk.test.ai</a>.
	 * @throws IOException If there was an initialization error.
	 */
	public TestAiDriver(AppiumDriver<T> driver, String apiKey) throws IOException
	{
		this(driver, apiKey, null, null, true);
	}

	/**
	 * Convenience method, implicitly wait for the specified amount of time.
	 * 
	 * @param waitTime The number of seconds to implicitly wait.
	 * @return This {@code TestAiDriver}, for chaining convenience.
	 */
	public TestAiDriver<T> implicitlyWait(long waitTime)
	{
		driver.manage().timeouts().implicitlyWait(waitTime, TimeUnit.SECONDS);
		return this;
	}

	public WebDriver context(String name)
	{
		return driver.context(name);
	}

	public org.openqa.selenium.remote.Response execute(String command)
	{
		return driver.execute(command);
	}

	public org.openqa.selenium.remote.Response execute(String command, Map<String, ?> parameters)
	{
		return driver.execute(command, parameters);
	}

	public T findElement(By locator)
	{
		return driver.findElement(locator);
	}

	public T findElement(String by, String using)
	{
		return driver.findElement(by, using);
	}

	public String getContext()
	{
		return driver.getContext();
	}

	public Set<String> getContextHandles()
	{
		return driver.getContextHandles();
	}

	public ExecuteMethod getExecuteMethod()
	{
		return driver.getExecuteMethod();
	}

	public ScreenOrientation getOrientation()
	{
		return driver.getOrientation();
	}

	public URL getRemoteAddress()
	{
		return driver.getRemoteAddress();
	}

	public Map<String, Object> getStatus()
	{
		return driver.getStatus();
	}

	public boolean isBrowser()
	{
		return driver.isBrowser();
	}

	public Location location()
	{
		return driver.location();
	}

	public void rotate(DeviceRotation rotation)
	{
		driver.rotate(rotation);
	}

	public void rotate(ScreenOrientation orientation)
	{
		driver.rotate(orientation);
	}

	public DeviceRotation rotation()
	{
		return driver.rotation();
	}

	public void setLocation(Location location)
	{
		driver.setLocation(location);
	}

	public String toString()
	{
		return driver.toString();
	}

	public Object executeAsyncScript(String script, Object... args)
	{
		return driver.executeAsyncScript(script, args);
	}

	public Object executeScript(String script, Object... args)
	{
		return driver.executeScript(script, args);
	}

	public Capabilities getCapabilities()
	{
		return driver.getCapabilities();
	}

	public CommandExecutor getCommandExecutor()
	{
		return driver.getCommandExecutor();
	}

	public String getCurrentUrl()
	{
		return driver.getCurrentUrl();
	}

	public ErrorHandler getErrorHandler()
	{
		return driver.getErrorHandler();
	}

	public FileDetector getFileDetector()
	{
		return driver.getFileDetector();
	}

	public Keyboard getKeyboard()
	{
		return driver.getKeyboard();
	}

	public Mouse getMouse()
	{
		return driver.getMouse();
	}

	public String getPageSource()
	{
		return driver.getPageSource();
	}

	public <X> X getScreenshotAs(OutputType<X> outputType)
	{
		return driver.getScreenshotAs(outputType);
	}

	public SessionId getSessionId()
	{
		return driver.getSessionId();
	}

	public String getTitle()
	{
		return driver.getTitle();
	}

	public String getWindowHandle()
	{
		return driver.getWindowHandle();
	}

	public Set<String> getWindowHandles()
	{
		return driver.getWindowHandles();
	}

	public Options manage()
	{
		return driver.manage();
	}

	public Navigation navigate()
	{
		return driver.navigate();
	}

	public void perform(Collection<Sequence> actions)
	{
		driver.perform(actions);
	}

	public void quit()
	{
		driver.quit();
	}

	public void resetInputState()
	{
		driver.resetInputState();
	}

	public void setErrorHandler(ErrorHandler handler)
	{
		driver.setErrorHandler(handler);
	}

	public void setFileDetector(FileDetector detector)
	{
		driver.setFileDetector(detector);
	}

	public void setLogLevel(Level level)
	{
		driver.setLogLevel(level);
	}

	public TargetLocator switchTo()
	{
		return driver.switchTo();
	}

	/**
	 * Attempts to find an element by accessibility id.
	 * 
	 * @param using The accessibility id of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByAccessibilityId(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "accessibility_id", driver::findElementByAccessibilityId);
	}

	/**
	 * Attempts to find an element by accessibility id.
	 * 
	 * @param using The accessibility id of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByAccessibilityId(String using)
	{
		return findElementByAccessibilityId(using, null);
	}

	/**
	 * Attempts to find all elements with the matching accessibility id.
	 * 
	 * @param using The accessibility id of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List<T> findElementsByAccessibilityId(String using)
	{
		return driver.findElementsByAccessibilityId(using);
	}

	/**
	 * Attempts to find an element by class name.
	 * 
	 * @param using The class name of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByClassName(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "class_name", driver::findElementByClassName);
	}

	/**
	 * Attempts to find an element by class name.
	 * 
	 * @param using The class name of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByClassName(String using)
	{
		return findElementByClassName(using, null);
	}

	/**
	 * Attempts to find all elements with the matching class name.
	 * 
	 * @param using The class name of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsByClassName(String using)
	{
		return driver.findElementsByClassName(using);
	}

	/**
	 * Attempts to find an element by css selector.
	 * 
	 * @param using The css selector of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByCssSelector(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "class_name", driver::findElementByCssSelector);
	}

	/**
	 * Attempts to find an element by css selector.
	 * 
	 * @param using The css selector of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByCssSelector(String using)
	{
		return findElementByCssSelector(using, null);
	}

	/**
	 * Attempts to find all elements with the matching css selector.
	 * 
	 * @param using The css selector of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsByCssSelector(String using)
	{
		return driver.findElementsByCssSelector(using);
	}

	/**
	 * Attempts to find an element by id.
	 * 
	 * @param using The id of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementById(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "class_name", driver::findElementById);
	}

	/**
	 * Attempts to find an element by id.
	 * 
	 * @param using The id of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementById(String using)
	{
		return findElementById(using, null);
	}

	/**
	 * Attempts to find all elements with the matching id.
	 * 
	 * @param using The id of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsById(String using)
	{
		return driver.findElementsById(using);
	}

	/**
	 * Attempts to find an element by link text.
	 * 
	 * @param using The link text of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByLinkText(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "class_name", driver::findElementByLinkText);
	}

	/**
	 * Attempts to find an element by link text.
	 * 
	 * @param using The link text of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByLinkText(String using)
	{
		return findElementByLinkText(using, null);
	}

	/**
	 * Attempts to find all elements with the matching link text.
	 * 
	 * @param using The link text of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsByLinkText(String using)
	{
		return driver.findElementsByLinkText(using);
	}

	/**
	 * Attempts to find an element by name.
	 * 
	 * @param using The name of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByName(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "name", driver::findElementByName);
	}

	/**
	 * Attempts to find an element by name.
	 * 
	 * @param using The name of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByName(String using)
	{
		return findElementByName(using, null);
	}

	/**
	 * Attempts to find all elements with the matching name.
	 * 
	 * @param using The name of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsByName(String using)
	{
		return driver.findElementsByName(using);
	}

	/**
	 * Attempts to find an element by partial link text.
	 * 
	 * @param using The partial link text of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByPartialLinkText(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "name", driver::findElementByPartialLinkText);
	}

	/**
	 * Attempts to find an element by partial link text.
	 * 
	 * @param using The partial link text of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByPartialLinkText(String using)
	{
		return findElementByPartialLinkText(using, null);
	}

	/**
	 * Attempts to find all elements with the matching partial link text.
	 * 
	 * @param using The partial link text of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsByPartialLinkText(String using)
	{
		return driver.findElementsByPartialLinkText(using);
	}

	/**
	 * Attempts to find an element by tag name.
	 * 
	 * @param using The tag name of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByTagName(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "name", driver::findElementByTagName);
	}

	/**
	 * Attempts to find an element by tag name.
	 * 
	 * @param using The tag name of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByTagName(String using)
	{
		return findElementByTagName(using, null);
	}

	/**
	 * Attempts to find all elements with the matching tag name.
	 * 
	 * @param using The tag name of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsByTagName(String using)
	{
		return driver.findElementsByTagName(using);
	}

	/**
	 * Attempts to find an element by xpath.
	 * 
	 * @param using The xpath of the element to find
	 * @param elementName The label name of the element to be classified. Optional, set {@code null} to auto generate an element name.
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByXPath(String using, String elementName)
	{
		return findElementByGeneric(using, elementName, "xpath", driver::findElementByXPath);
	}

	/**
	 * Attempts to find an element by xpath.
	 * 
	 * @param using The xpath of the element to find
	 * @return The element that was found. Raises an exception otherwise.
	 */
	public T findElementByXPath(String using)
	{
		return findElementByXPath(using, null);
	}

	/**
	 * Attempts to find all elements with the matching xpath.
	 * 
	 * @param using The xpath of the elements to find.
	 * @return A {@code List} with any elements that were found, or an empty {@code List} if no matches were found.
	 */
	public List findElementsByXPath(String using)
	{
		return driver.findElementsByXPath(using);
	}

	/**
	 * Finds an element by {@code elementName}.
	 * 
	 * @param elementName The label name of the element to be classified.
	 * @return An element associated with {@code elementName}. Throws NoSuchElementException otherwise.
	 */
	public MobileElement findByElementName(String elementName)
	{
		ClassifyResult r = classify(elementName);
		if (r.e == null)
			throw new NoSuchElementException(r.msg);

		return r.e;
	}

	/**
	 * Shared {@code findElementBy} functionality. This serves as the base logic for most find by methods exposed to the end user.
	 * 
	 * @param using The search term to use when looking for an element.
	 * @param elementName The label name of the element to be classified. This is what the element will be stored under in the test.ai db.
	 * @param shortcode The short identifier for the type of lookup being performed. This will be used to aut-generate an {@code elementName} if the user did not specify one.
	 * @param fn The appium function to call with {@code using}, which will be used to fetch what Appium thinks is the target element.
	 * @return The TestAiElement
	 */
	private T findElementByGeneric(String using, String elementName, String shortcode, Function<String, T> fn)
	{
		if (elementName == null)
			elementName = String.format("element_name_by_%s_%s", shortcode, using.replace('.', '_'));

		elementName = elementName.replace(' ', '_');

		try
		{
			T driverElement = fn.apply(using);
			if (driverElement != null)
			{
				ClassifyResult result = classify(elementName);
				updateElement(driverElement, result.key, elementName, true);
			}

			return driverElement;
		}
		catch (Throwable x)
		{
			log.info("Element '{}' was not found by Appium, trying with test.ai...", elementName);

			ClassifyResult result = classify(elementName);
			if (result.e != null)
				return (T) result.e;

			log.error("test.ai was also unable to find the element with name '{}'", elementName);

			throw x;
		}
	}

	/**
	 * Updates the entry for an element as it is known to the test.ai servers.
	 * 
	 * @param elem The element to update
	 * @param key The key associated with this element
	 * @param elementName The name associated with this element
	 * @param trainIfNecessary Set {@code true} if the model on the server should also be trained with this element.
	 */
	private void updateElement(T elem, String key, String elementName, boolean trainIfNecessary)
	{
		Rectangle rect = ((MobileElement) elem).getRect();
		HashMap<String, String> form = CollectionUtils.keyValuesToHM("key", key, "api_key", apiKey, "run_id", runID, "x", Integer.toString(rect.x), "y", Integer.toString(rect.y), "width",
				Integer.toString(rect.width), "height", Integer.toString(rect.height), "multiplier", Double.toString(multiplier), "train_if_necessary", Boolean.toString(trainIfNecessary));

		try (Response r = NetUtils.basicPOST(client, serverURL, "add_action", form))
		{
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Perform additional classification on an element by querying the test.ai server.
	 * 
	 * @param elementName The name of the element to run classification on.
	 * @return The result of the classification.
	 */
	private ClassifyResult classify(String elementName)
	{
		if (testCaseName != null)
			return null; // TODO: add test case creation/interactive mode

		String pageSource = "", msg = "test.ai driver exception", key = null;
		try
		{
			pageSource = driver.getPageSource();
		}
		catch (Throwable e)
		{

		}

		try
		{
			String screenshotBase64 = driver.getScreenshotAs(OutputType.BASE64);
			// Files.write(Paths.get("/tmp/scnshot.png"), Base64.getMimeDecoder().decode(screenshotBase64));

			JsonObject r = JsonUtils.responseAsJson(NetUtils.basicPOST(client, serverURL, "classify",
					CollectionUtils.keyValuesToHM("screenshot", screenshotBase64, "source", pageSource, "api_key", apiKey, "label", elementName, "run_id", runID)));

			key = JsonUtils.stringFromJson(r, "key");

			if (JsonUtils.booleanFromJson(r, "success"))
			{
				log.info("Successfully classified: {}", elementName);
				return new ClassifyResult(new TestAiElement(r.get("elem").getAsJsonObject(), driver, multiplier), key);
			}

			String rawMsg = JsonUtils.stringFromJson(r, "message");
			if (rawMsg != null)
			{
				String cFailedBase = "Classification failed for element_name: ";

				if (rawMsg.contains("Please label") || rawMsg.contains("Did not find"))
					msg = String.format("%s%s - Please visit %s/label/%s to classify", cFailedBase, elementName, serverURL, elementName);
				else if (rawMsg.contains("frozen label"))
					msg = String.format("%s%s - However this element is frozen, so no new screenshot was uploaded. Please unfreeze the element if you want to add this screenshot to training", cFailedBase,
							elementName);
				else
					msg = String.format("%s: Unknown error, here was the API response: %s", msg, r);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		log.warn(msg);
		return new ClassifyResult(null, key, msg);
	}

	/**
	 * Simple container for encapsulating results of calls to {@code classify()}.
	 * 
	 * @author Alexander Wu (alec@test.ai)
	 *
	 */
	private static class ClassifyResult
	{
		/**
		 * The TestAiElement created by the call to classify
		 */
		public TestAiElement e;

		/**
		 * The key returned by the call to classify
		 */
		public String key;

		/**
		 * The message associated with this result
		 */
		public String msg;

		/**
		 * Constructor, creates a new ClassifyResult.
		 * 
		 * @param e The TestAiElement to to use
		 * @param key The key to use
		 * @param msg The message to associate with this result
		 */
		ClassifyResult(TestAiElement e, String key, String msg)
		{
			this.e = e;
			this.key = key;
			this.msg = msg;
		}

		/**
		 * Constructor, creates a new ClassifyResult, where the {@code msg} is set to the empty String by default.
		 * 
		 * @param e
		 * @param key
		 */
		ClassifyResult(TestAiElement e, String key)
		{
			this(e, key, "");
		}
	}
}
