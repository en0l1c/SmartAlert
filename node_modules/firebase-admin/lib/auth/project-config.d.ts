/*! firebase-admin v11.6.0 */
import { SmsRegionConfig, MultiFactorConfig, MultiFactorAuthServerConfig } from './auth-config';
/**
 * Interface representing the properties to update on the provided project config.
 */
export interface UpdateProjectConfigRequest {
    /**
     * The SMS configuration to update on the project.
     */
    smsRegionConfig?: SmsRegionConfig;
    /**
     * The multi-factor auth configuration to update on the project.
     */
    multiFactorConfig?: MultiFactorConfig;
}
/**
 * Response received from getting or updating a project config.
 * This object currently exposes only the SMS Region config.
 */
export interface ProjectConfigServerResponse {
    smsRegionConfig?: SmsRegionConfig;
    mfa?: MultiFactorAuthServerConfig;
}
/**
 * Request sent to update project config.
 * This object currently exposes only the SMS Region config.
 */
export interface ProjectConfigClientRequest {
    smsRegionConfig?: SmsRegionConfig;
    mfa?: MultiFactorAuthServerConfig;
}
/**
* Represents a project configuration.
*/
export declare class ProjectConfig {
    /**
     * The SMS Regions Config for the project.
     * Configures the regions where users are allowed to send verification SMS.
     * This is based on the calling code of the destination phone number.
     */
    readonly smsRegionConfig?: SmsRegionConfig;
    /**
     * The project's multi-factor auth configuration.
     * Supports only phone and TOTP.
     */ private readonly multiFactorConfig_?;
    /**
     * The multi-factor auth configuration.
     */
    get multiFactorConfig(): MultiFactorConfig | undefined;
    /**
     * Validates a project config options object. Throws an error on failure.
     *
     * @param request - The project config options object to validate.
     */
    private static validate;
    /**
     * Returns a JSON-serializable representation of this object.
     *
     * @returns A JSON-serializable representation of this object.
     */
    toJSON(): object;
}
