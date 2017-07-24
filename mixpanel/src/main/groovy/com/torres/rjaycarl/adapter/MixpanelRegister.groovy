package com.torres.rjaycarl.adapter

import org.springframework.util.StringUtils

class MixpanelRegister {

    /**
     * Save your credentials here. This is not part of the API.
     * <br/><a href='https://mixpanel.com/account'>See External Resources here</a>
     * @param alias The keyword for your alias preconfigured alias
     * @param distinctId A string uniquely identifying the individual.
     * 			If no profile exists for the given id, a new one will be created.
     * @param apiKey API Key provided by mixpanel
     * @param apiSecret API Secret provided by mixpanel
     * @return Save access details
     */
    static String saveAccessDetails( String alias, String apiKey,
                                     String apiSecret, String token ){
        if (StringUtils.isBlank( alias ))
            return 'ERROR: Please enter an alias'
        if ( StringUtils.isBlank( apiKey ) )
            return 'ERROR: Please enter an apiKey'
        if (StringUtils.isBlank( apiSecret ))
            return 'ERROR: Please enter an API Secret'
        if (StringUtils.isBlank( token ))
            return 'ERROR: Please enter a token'

        alias.mixpanelSaveCredentials( apiKey, apiSecret, token )
        "redirect:http://localhost:8080/package/"
    }
}
