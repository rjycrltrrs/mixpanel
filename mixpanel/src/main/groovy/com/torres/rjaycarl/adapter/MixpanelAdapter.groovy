package com.torres.rjaycarl.adapter

import org.hibernate.validator.constraints.NotBlank
import org.json.JSONObject
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.*

import javax.ws.rs.DefaultValue

@ResponseBody
@RequestMapping( value = 'mixpanel', produces = ['application/json'] )
class MixpanelAdapter {

    /**
     *
     * @param alias The keyword for your alias preconfigured alias
     * @param distinctId A string uniquely identifying the individual cause associated with this event
     * 			(for example, the user id of a signing-in user, or the hostname of a server)
     * @param eventName A human readable name for the event, for example "Purchase", or "Threw Exception"
     * @param event A String to put in org.json.JSONObject properties
     * @param value An Object to put in org.json.JSONObject properties
     * @return APIResponse
     */
    @ResponseBody
    @RequestMapping( value = 'create-new-post', method = [RequestMethod.POST] )
    public APIResponse createNewPost(
            @NotBlank
            @DefaultValue( 'test' )
            @RequestParam String alias,

            @NotBlank
            @RequestParam String distinctId,

            @NotBlank
            @RequestParam String eventName,

            @NotBlank
            @RequestParam String event,

            @NotBlank
            @RequestParam String value ){
        JSONObject properties = new JSONObject()
        properties.put( event, value )
        properties.put( "\$time", new Date() )
        def result = alias.mixpanelCreateNewPost( distinctId, eventName, properties )
        new APIResponse( 'OK', result )
    }

    /**
     *
     * @param alias The keyword for your alias preconfigured alias
     * @param distinctId A string uniquely identifying the people analytics profile to change,
     * 			for example, a user id of an app, or the hostname of a server.
     * 			If no profile exists for the given id, a new one will be created.
     * @param event Use special properties in profile updates
     * 			$first_name and $last_name - Should be set to the first and last name of the user
     * 			represented by the profile. If these are set, the full name of the user will be displayed in Mixpanel reports.
     * 			$name - Can be set to the user's full name as an alternative to having two separate first and last name properties.
     * 			$created - The time when the user created their account. This should be expressed as a Mixpanel date string.
     * 			param value can be null.
     * 			$email - The user's email address as a string,e.g. "joe.doe@example.com".
     * 			Mixpanel will use the "$email" property when sending email notifications to your users,
     * 			and for displaying the user's gravatar image in reports.
     * 			$phone - The user's phone number as a string, e.g. "4805551212".
     * 			Mixpanel will use the "$phone" property when sending SMS messages to your users.
     * @param value An Object to put in org.json.JSONObject properties.
     * @return APIResponse
     */
    @ResponseBody
    @RequestMapping( value = 'set-profile-properties', method = [RequestMethod.POST] )
    public APIResponse setProfileProperties(
            @NotBlank
            @DefaultValue( 'test' )
            @RequestParam String alias,

            @NotBlank
            @RequestParam String distinctId,

            @NotBlank
            @RequestParam String event,

            @NotBlank
            @RequestParam String value ){
        if( event.equals("\$created") )
            value = new Date()

        JSONObject properties = new JSONObject()
        properties.put( event, value )
        JSONObject modifiers = new JSONObject()
        modifiers.put( "\$time", new Date() )
        def result = alias.mixpanelSetProfileProperties( distinctId, properties, modifiers )
        new APIResponse( 'OK', result )
    }

    /**
     *
     * @param alias The keyword for your alias preconfigured alias
     * @param distinctId A string uniquely identifying the people analytics profile to change,
     * 			for example, a user id of an app, or the hostname of a server.
     * 			If no profile exists for the given id, a new one will be created.
     * @param event A String to put in org.json.JSONObject properties
     * @param value An Object to put in org.json.JSONObject properties
     * @return APIResponse
     */
    @ResponseBody
    @RequestMapping( value = 'append-to-list-properties', method = [RequestMethod.POST] )
    public APIResponse appendToListProperties(
            @NotBlank
            @DefaultValue( 'test' )
            @RequestParam String alias,

            @NotBlank
            @RequestParam String distinctId,

            @NotBlank
            @RequestParam String event,

            @NotBlank
            @RequestParam String value ){
        JSONObject properties = new JSONObject()
        properties.put( event, value )
        JSONObject modifiers = new JSONObject()
        modifiers.put( "\$time", new Date() )
        def result = alias.mixpanelAppendingToListProperties( distinctId, properties, modifiers )
        new APIResponse( 'OK', result )
    }

    /**
     *
     * @param alias The keyword for your alias preconfigured alias
     * @param distinctId An identifier associated with a People Analytics profile
     * @param amount A double revenue amount. Positive amounts represent income for your business.
     * @return APIResponse
     */
    @ResponseBody
    @RequestMapping( value = 'track-revenue', method = [RequestMethod.POST] )
    public APIResponse trackingRevenue(
            @NotBlank
            @DefaultValue( 'test' )
            @RequestParam String alias,

            @NotBlank
            @RequestParam String distinctId,

            @NotBlank
            @RequestParam double amount ){
        def result = alias.mixpanelTrackingRevenue( distinctId, amount, null, null )
        new APIResponse( 'OK', result )
    }

    /**
     *
     * @param alias The keyword for your alias preconfigured alias
     * @param distinctId A string uniquely identifying the people analytics profile to delete
     * @return If successful, this method returns an empty response body.
     */
    @ResponseBody
    @RequestMapping( value = 'delete-user', method = [RequestMethod.DELETE] )
    public APIResponse deleteUser(
            @NotBlank
            @DefaultValue( 'test' )
            @RequestParam String alias,

            @NotBlank
            @RequestParam String distinctId ){
        def result = alias.mixpanelDeleteUser( distinctId, null )
        new APIResponse( 'OK', result )
    }
}
