function springMediatorValidate() {
        if (document.getElementById('beanName') != undefined) {
            var val = document.getElementById('beanName');
            if (val && val.value == "") {
                CARBON.showWarningDialog(springi18n["mediator.spring.bean.name.empty"]);
                return false;
            }
        }
     if (document.getElementById('beanKey') != undefined) {
            val = document.getElementById('beanKey');
            if (val && val.value == "") {
                CARBON.showWarningDialog(springi18n["mediator.spring.bean.key.empty"]);
                return false;
            }
        }
    return true;
}