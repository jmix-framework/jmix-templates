package ${project_rootPackage}.screen.user

import ${project_rootPackage}.entity.User
import io.jmix.core.EntityStates
import io.jmix.ui.Notifications
import io.jmix.ui.component.PasswordField
import io.jmix.ui.component.TextField
import io.jmix.ui.navigation.Route
import io.jmix.ui.screen.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder

@UiController("${normalizedPrefix_underscore}User.edit")
@UiDescriptor("user-edit.xml")
@EditedEntityContainer("userDc")
@Route(value = "users/edit", parentPrefix = "users")
open class UserEdit : StandardEditor<User>() {

    @Autowired
    private var entityStates: EntityStates? = null

    @Autowired
    private var passwordEncoder: PasswordEncoder? = null

    @Autowired
    private var passwordField: PasswordField? = null

    @Autowired
    private var usernameField: TextField<String>? = null

    @Autowired
    private var confirmPasswordField: PasswordField? = null

    @Autowired
    private var notifications: Notifications? = null

    @Autowired
    private var messageBundle: MessageBundle? = null

    @Subscribe
    fun onInitEntity(event: InitEntityEvent<User>?) {
        usernameField?.isEditable = true
        passwordField?.isVisible = true
        confirmPasswordField?.isVisible = true
    }

    @Subscribe
    fun onAfterShow(event: AfterShowEvent?) {
        if (entityStates?.isNew(editedEntity) == true) {
            usernameField?.focus()
        }
    }

    @Subscribe
    protected fun onBeforeCommit(event: BeforeCommitChangesEvent) {
        if (entityStates?.isNew(editedEntity) == true) {
            if (passwordField?.value != confirmPasswordField?.value) {
                notifications?.create(Notifications.NotificationType.WARNING)
                    ?.withCaption(messageBundle?.getMessage("passwordsDoNotMatch") ?: "")
                    ?.show()
                event.preventCommit()
            }
            editedEntity.password = passwordEncoder?.encode(passwordField?.value)
        }
    }
}