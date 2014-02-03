package org.wso2.carbon.business.messaging.salesforce.mediator.ui;

/**
 * This class stores all UI related input parameter type information
 */
public class InputTypeUI extends UIType {
    /**
     * indicates required input parametr to the ui
     */
    private boolean isRequired;
    /**
     * indicates the ability to add more input types within ui
     */
    private boolean isExpandable = false;
    /**
     * indicates the special wrapped type to the ui
     */
    private boolean isComplexType = false;

    /**
     * model reference
     */
    private InputType inputType;


    public InputTypeUI(InputType input) {
        this.inputType = input;

    }

    /**
     * @return the isRequired
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * @param isRequired the isRequired to set
     */
    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    /**
     * @return input model
     */
    public InputType getInputType() {
        return inputType;
    }

    /**
     * @return whether user could add more inputs ; used for controlling ui behaviour for collection
     *         inputs
     */
    public boolean isExpandable() {
        return isExpandable;
    }

    /**
     * @param expandable
     */
    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    /**
     * @return true if this is a complex type input false othewise
     */
    public boolean isComplexType() {
        return isComplexType;
    }

    /**
     * @param complexType
     */
    public void setComplexType(boolean complexType) {
        isComplexType = complexType;
    }
}