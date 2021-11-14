import {UserCredentials} from "../../model/User";
import {ConferenceInfo} from "../../model/ConferenceInfo";
import {ClassroomEvent} from "./ClassroomEvent";

export class InvitationEvent extends ClassroomEvent {
    inviter: UserCredentials | undefined
    invitee: UserCredentials | undefined
    conferenceInfo: ConferenceInfo | undefined

    constructor() {
        super("InvitationEvent");
    }
}
