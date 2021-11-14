import {ConferenceInfo} from "../../model/ConferenceInfo";
import {ClassroomEvent} from "./ClassroomEvent";

export enum ConferenceAction {
    CREATE = "CREATE",
    CLOSE = "CLOSE",
    PLENARY = "PLENARY",
    VISIBILITY = "VISIBILITY",
    USER_CHANGE = "USER_CHANGE"
}

export class ConferenceEvent extends ClassroomEvent {
    conferenceInfo: ConferenceInfo | undefined
    conferenceAction: ConferenceAction | undefined

    constructor() {
        super("ConferenceEvent");
    }
}
