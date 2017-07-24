package com.torres.rjaycarl.connector

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import com.mixpanel.mixpanelapi.ClientDelivery
import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import com.mixpanel.mixpanelapi.MixpanelMessageException
import com.mixpanel.mixpanelapi.MixpanelServerException
import com.toro.licensing.LicenseManager

import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import com.torres.rjaycarl.request.ArrayProperties
import com.torres.rjaycarl.request.LongProperty

/**
 * <a href='https://mixpanel.com/help/reference'>External Reference</a>
 * @author rjay.torres
 *
 */

@CompileStatic
@SuppressWarnings( ['DuplicateStringLiteral', 'DuplicateNumberLiteral', 'UnnecessaryGetter'] )
class MixpanelConnector {

    static ClientDelivery delivery
    static MixpanelAPI mixpanel
    static MessageBuilder messageBuilder
    static JSONObject event
    final static Map API_KEY_MAP

    static {
        delivery = new ClientDelivery()
        mixpanel = new MixpanelAPI()
        API_KEY_MAP = [ : ]
    }

    /**
     * <br/><a href='https://mixpanel.com/account'>See External Resources here</a>
     * @param alias The keyword for your alias
     * @return messageBuilder
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    static MessageBuilder mixpanelNewInstance( String alias ) {
        LicenseManager.getInstance().getLicense()
        String[] key = mixpanelGetApiKey( alias )
        messageBuilder = new MessageBuilder( "${key[2]}" )
        messageBuilder
    }

    /**
     * Save your credentials here. This is not part of the API.
     * <br/><a href='https://mixpanel.com/account'>See External Resources here</a>
     * @param alias The keyword for your alias preconfigured alias
     * @param distinctId A string uniquely identifying the individual.
     * 			If no profile exists for the given id, a new one will be created.
     * @param apiKey API Key provided by mixpanel
     * @param apiSecret API Secret provided by mixpanel
     * @param token Token provided by Mixpanel
     * @return Credential needed for authorization
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    static String mixpanelSaveCredentials( String alias, String apiKey,
                                           String apiSecret, String token ){
        if (StringUtils.isBlank( alias ))
            return 'ERROR: Please enter an alias'
        if ( StringUtils.isBlank( apiKey ) )
            return 'ERROR: Please enter an apiKey'
        if (StringUtils.isBlank( apiSecret ))
            return 'ERROR: Please enter an API Secret'
        if (StringUtils.isBlank( token ))
            return 'ERROR: Please enter a token'

        "mixpanel.${alias.trim()}".saveTOROProperty(
                "${apiKey.trim()}~${apiSecret.trim()}~${token.trim()}")
        "Successfully saved alias ${alias.trim()}."
    }

    /**
     * Gets API key
     * <br/><a href='https://mixpanel.com/account'>See External Resources here</a>
     * @param apiKeyAlias The keyword for your alias
     * @return Array of strings
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    static String[] mixpanelGetApiKey( String apiKeyAlias ) {
        String[] output = API_KEY_MAP."mixpanel.${apiKeyAlias}"
        if ( output ) {
            return output
        }
        String tempKey = "mixpanel.${apiKeyAlias}".getTOROProperty()
        if ( !tempKey )
            throw new MixpanelMessageException(
                    "No Mixpanel credentials found for alias ${apiKeyAlias}" )
        String[] tokens = tempKey.split( '~' )
        API_KEY_MAP.apiKeyAlias = tokens
        tokens
    }

    /**
     * Creates a message tracking an event, for consumption by MixpanelAPI
     * See:
     *
     *    <br/><a href='http://blog.mixpanel.com/2012/09/12/best-practices-updated/'>See External Resources here</a>
     *
     * for a detailed discussion of event names, distinct ids, event properties, and how to use them
     * 			to get the most out of your metrics.
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the individual cause associated with this event
     * 			(for example, the user id of a signing-in user, or the hostname of a server)
     * @param eventName A human readable name for the event, for example "Purchase", or "Threw Exception"
     * @param properties A JSONObject associating properties with the event.
     * 			These are useful for reporting and segmentation of events.
     * 			It is often useful not only to include properties of the event itself
     * 			(for example { 'Item Purchased' : 'Hat' } or { 'ExceptionType' : 'OutOfMemory' }),
     * 			but also properties associated with the identified user
     * 			(for example { 'MemberSince' : '2012-01-10' } or { 'TotalMemory' : '10TB' })
     * @return The created post on Activity Feed of Mixpanel
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelCreateNewPost( String alias, String distinctId,
                                             String eventName, JSONObject properties ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.event(
                distinctId, eventName, properties)

        delivery.addMessage( event )
        mixpanel.deliver( delivery )
        event
    }

    /**
     * Sets a People Analytics property on the profile associated with
     * the given distinctId. When sent, this message will overwrite any
     * existing values for the given properties. So, to set some properties
     * on user 12345, one might call:
     * <pre>
     * {@code
     *     JSONObject userProperties = new JSONObject();
     *     userProperties.put("Company", "Uneeda Medical Supply");
     *     userProperties.put("Easter Eggs", "Hatched");
     *     JSONObject message = messageBuilder.set("12345", userProperties);
     *     mixpanelApi.sendMessage(message);
     * }
     * </pre>
     * <br/><a href='https://mixpanel.com/report'>See External Resources here</a>
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the people analytics profile to change,
     * 			for example, a user id of an app, or the hostname of a server.
     * 			If no profile exists for the given id, a new one will be created.
     * @param properties A collection of properties to set on the associated profile.
     * 			Each key in the properties argument will be updated on on the people profile
     * @param modifiers Modifiers associated with the update message.
     * 			(for example "$time" or "$ignore_time").
     * 			This can be null- if non-null, the keys and values in the modifiers object
     * 			will be associated directly with the update.
     * @return The created properties to Properties of the distinctId in Mixpanel
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelSetProfileProperties( String alias, String distinctId,
                                                    JSONObject properties, JSONObject modifiers ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.set(
                distinctId, properties, modifiers )

        mixpanel.sendMessage( event )
        event
    }

    /**
     * Sets a People Analytics property on the profile associated with
     * the given distinctId, only if that property is not already set
     * on the associated profile. So, to set a new property on
     * on user 12345 if it is not already present, one might call:
     * <pre>
     * {@code
     *     JSONObject userProperties = new JSONObject();
     *     userProperties.put("Date Began", "2014-08-16");
     *
     *     // "Date Began" will not be overwritten, but if it isn't already
     *     // present it will be set when we send this message.
     *     JSONObject message = messageBuilder.setOnce("12345", userProperties);
     *     mixpanelApi.sendMessage(message);
     * }
     * </pre>
     * <br/><a href='https://mixpanel.com/report'>See External Resources here</a>
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the people analytics profile to change,
     * 			for example, a user id of an app, or the hostname of a server.
     * 			If no profile exists for the given id, a new one will be created.
     * @param properties A collection of properties to set on the associated profile.
     * 			Each key in the properties argument will be updated on on the people profile
     * @param modifiers Modifiers associated with the update message.
     * 			(for example "$time" or "$ignore_time").
     * 			This can be null- if non-null, the keys and values in the modifiers object
     * 			will be associated directly with the update.
     * @return The created properties to Properties of the distinctId in Mixpanel
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelSetOnceProfileProperties( String alias, String distinctId,
                                                        JSONObject properties, JSONObject modifiers ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.setOnce(
                distinctId, properties, modifiers )

        mixpanel.sendMessage( event )
        event
    }

    /**
     * <br/><a href='https://mixpanel.com/help/reference'>See External Resources here</a>
     * For each key and value in the properties argument, adds that amount
     * to the associated property in the People Analytics profile with the given distinct id.
     * So, to maintain a login count for user 12345, one might run the following code
     * at every login:
     * <pre>
     * {@code
     *    Map<String, Long> updates = new HashMap<String, Long>();
     *    updates.put('Logins', 1);
     *    JSONObject message = messageBuilder.set("12345", updates);
     *    mixpanelApi.sendMessage(message);
     * }
     * </pre>
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the people analytics profile to change.
     * 			If no profile exists for the given id, a new one will be created.
     * @param properties A collection of properties to change on the associated profile,
     * 			each associated with a numeric value.
     * @param modifiers Modifiers associated with the update message.
     * 			(for example "$time" or "$ignore_time").
     * @return The incremented/decremented value of property
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelIncrementNumericProperties( String alias, String distinctId,
                                                          LongProperty prop, JSONObject modifiers ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.increment(
                distinctId, prop.properties, modifiers )

        mixpanel.sendMessage( event )
        event
    }

    /**
     * <br/><a href='https://mixpanel.com/help/reference'>See External Resources here</a>
     * For each key and value in the properties argument, attempts to append
     * that value to a list associated with the key in the identified People Analytics profile.
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the people analytics profile to change,
     * 			for example, a user id of an app, or the hostname of a server.
     * 			If no profile exists for the given id, a new one will be created.
     * @param properties A collection of properties to set on the associated profile.
     * @param modifiers Modifiers associated with the update message. (for example "$time" or "$ignore_time").
     *            this can be null- if non-null, the keys and values in the modifiers
     *            object will be associated directly with the update.
     * @return The value to append in the property
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelAppendingToListProperties( String alias, String distinctId,
                                                         JSONObject properties, JSONObject modifiers ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.append(
                distinctId, properties, modifiers )

        mixpanel.sendMessage( event )
        event
    }

    /**
     * <br/><a href='https://mixpanel.com/help/reference'>See External Resources here</a>
     * Tracks revenue associated with the given distinctId.
     * @param alias The keyword for your alias
     * @param distinctId An identifier associated with a People Analytics profile
     * @param amount A double revenue amount. Positive amounts represent income for your business.
     * @param properties A collection of properties to set on the associated profile.
     * @param modifiers Modifiers associated with the update message. (for example "$time" or "$ignore_time").
     *            this can be null- if non-null, the keys and values in the modifiers
     *            object will be associated directly with the update.
     * @return The tracked revenue of your business
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelTrackingRevenue( String alias, String distinctId,
                                               double amount, JSONObject properties, JSONObject modifiers ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.trackCharge(
                distinctId, amount, properties, modifiers )

        mixpanel.sendMessage( event )
        event
    }

    /**
     * <br/><a href='https://mixpanel.com/help/reference'>See External Resources here</a>
     * Deletes the People Analytics profile associated with
     * the given distinctId.
     *
     * <pre>
     * {@code
     *     JSONObject message = messageBuilder.delete("12345");
     *     mixpanelApi.sendMessage(message);
     * }
     * </pre>
     *
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the people analytics profile to delete
     * @param modifiers Modifiers associated with the update message. (for example "$time" or "$ignore_time").
     *            this can be null- if non-null, the keys and values in the modifiers
     *            object will be associated directly with the update.
     * @return If successful, this method returns an empty response body.
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelDeleteUser( String alias, String distinctId,
                                          JSONObject modifiers ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.delete( distinctId, modifiers )

        mixpanel.sendMessage( event )
        event
    }

    /**
     * <br/><a href='https://mixpanel.com/help/reference'>See External Resources here</a>
     * Removes the properties named in propertyNames from the profile identified by distinctId.
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the people analytics profile to unset
     * @param propertyNames A collection of properties to remove on the associated profile.
     * @param modifiers Modifiers associated with the update message. (for example "$time" or "$ignore_time").
     *            this can be null- if non-null, the keys and values in the modifiers
     *            object will be associated directly with the update.
     * @return If successful, this method removes the property name
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelUnsetPropertyFromProfile( String alias, String distinctId,
                                                        Collection<String> propertyNames, JSONObject modifiers) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.unset(
                distinctId, propertyNames, modifiers)

        mixpanel.sendMessage( event )
        event
    }

    /**
     * <br/><a href='https://mixpanel.com/help/reference'>See External Resources here</a>
     * Merges list-valued properties into a user profile.
     * The list values in the given are merged with the existing list on the user profile,
     * ignoring duplicate list values.
     * @param alias The keyword for your alias
     * @param distinctId A string uniquely identifying the people analytics profile to unite
     * @param properties Collection of properties to set on the associated profile.
     * @param modifiers Modifiers associated with the update message. (for example "$time" or "$ignore_time").
     *            this can be null- if non-null, the keys and values in the modifiers
     *            object will be associated directly with the update.
     * @return The merges list-valued properties into a user profile
     * @throws MixpanelMessageException throws Exception
     * @throws MixpanelServerException throws Exception
     */
    static JSONObject mixpanelUnion( String alias, String distinctId,
                                     ArrayProperties properties, JSONObject modifiers ) throws MixpanelMessageException, MixpanelServerException{
        messageBuilder = mixpanelNewInstance( alias )
        event = messageBuilder.union(
                distinctId, properties.props, modifiers )

        mixpanel.sendMessage( event )
        event
    }
}
