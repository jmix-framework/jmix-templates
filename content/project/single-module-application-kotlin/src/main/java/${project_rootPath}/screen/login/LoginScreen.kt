package ${project_rootPackage}.screen.login

import io.jmix.core.MessageTools
import io.jmix.core.Messages
import io.jmix.securityui.authentication.AuthDetails
import io.jmix.securityui.authentication.LoginScreenSupport
import io.jmix.ui.JmixApp
import io.jmix.ui.Notifications
import io.jmix.ui.action.Action.ActionPerformedEvent
import io.jmix.ui.component.CheckBox
import io.jmix.ui.component.ComboBox
import io.jmix.ui.component.PasswordField
import io.jmix.ui.component.TextField
import io.jmix.ui.navigation.Route
import io.jmix.ui.screen.Screen
import io.jmix.ui.screen.Subscribe
import io.jmix.ui.screen.UiController
import io.jmix.ui.screen.UiDescriptor
import io.jmix.ui.security.UiLoginProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import java.util.*

@UiController("${normalizedPrefix_underscore}LoginScreen")
@UiDescriptor("login-screen.xml")
@Route(path = "login", root = true)
open class LoginScreen : Screen() {

    @Autowired
    private var usernameField: TextField<String>? = null

    @Autowired
    private var passwordField: PasswordField? = null

    @Autowired
    private var rememberMeCheckBox: CheckBox? = null

    @Autowired
    private var localesField: ComboBox<Locale>? = null

    @Autowired
    private var notifications: Notifications? = null

    @Autowired
    private var messages: Messages? = null

    @Autowired
    private var messageTools: MessageTools? = null

    @Autowired
    private var loginScreenSupport: LoginScreenSupport? = null

    @Autowired
    private var loginProperties: UiLoginProperties? = null

    @Autowired
    private var app: JmixApp? = null

    private var log = LoggerFactory.getLogger(LoginScreen::class.java)

    @Subscribe
    private fun onInit(event: InitEvent) {
        usernameField?.focus()
        initLocalesField()
        initDefaultCredentials()
    }

    private fun initLocalesField() {
        localesField?.apply {
            messageTools?.availableLocalesMap?.let { setOptionsMap(it) }
            value = app?.locale
        }
    }

    private fun initDefaultCredentials() {
        val defaultUsername = loginProperties?.defaultUsername
        if (!defaultUsername.isNullOrBlank() && "<disabled>" != defaultUsername) {
            usernameField?.setValue(defaultUsername)
        } else {
            usernameField?.value = ""
        }
        val defaultPassword = loginProperties?.defaultPassword
        if (!defaultPassword.isNullOrBlank() && "<disabled>" != defaultPassword) {
            passwordField?.value = defaultPassword
        } else {
            passwordField?.value = ""
        }
    }

    private fun getCaptionMessage(key: String) =
        messages?.getMessage(javaClass, key) ?: ""

    @Subscribe("submit")
    private fun onSubmitActionPerformed(event: ActionPerformedEvent) {
        login()
    }

    private fun login() {
        val username = usernameField?.value
        val password = passwordField?.value
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            notifications?.create(Notifications.NotificationType.WARNING)
                ?.withCaption(getCaptionMessage("emptyUsernameOrPassword"))
                ?.show()
            return
        }
        try {
            loginScreenSupport?.authenticate(
                AuthDetails.of(username, password)
                    .withLocale(localesField?.value)
                    .withRememberMe(rememberMeCheckBox?.isChecked ?: false), this
            )
        } catch (e: BadCredentialsException) {
            log.info("Login failed", e)
            notifications?.create(Notifications.NotificationType.ERROR)
                ?.withCaption(getCaptionMessage("loginFailed"))
                ?.withDescription(getCaptionMessage("badCredentials"))
                ?.show()
        } catch (e: DisabledException) {
            log.info("Login failed", e)
            notifications?.create(Notifications.NotificationType.ERROR)
                ?.withCaption(getCaptionMessage("loginFailed"))
                ?.withDescription(getCaptionMessage("badCredentials"))
                ?.show()
        } catch (e: LockedException) {
            log.info("Login failed", e)
            notifications?.create(Notifications.NotificationType.ERROR)
                ?.withCaption(getCaptionMessage("loginFailed"))
                ?.withDescription(getCaptionMessage("badCredentials"))
                ?.show()
        }
    }
}