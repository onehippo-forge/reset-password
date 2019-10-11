package org.onehippo.forge.resetpassword.frontend;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
/**
 * Exists as inner class of org.hippoecm.frontend.plugins.login.LoginPanel.
 * If ResetPassword becomes officially supported plugin it would make sense to create a class out of it for re-usability.
 */
public class AjaxAttributeModifier extends AttributeModifier {

    private String markupId;

    AjaxAttributeModifier(final String attribute, final IModel<String> replaceModel) {
        super(attribute, replaceModel);
    }

    @Override
    public void bind(final Component component) {
        markupId = component.getMarkupId();
    }

    void update(final AjaxRequestTarget target) {
        final String value = StringEscapeUtils.escapeJavaScript((String) getReplaceModel().getObject());
        target.appendJavaScript(String.format("$('#%s').attr('%s', '%s');", markupId, getAttribute(), value));
    }
}
