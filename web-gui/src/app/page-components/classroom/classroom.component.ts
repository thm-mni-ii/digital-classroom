import {
  Component, ComponentFactory,
  ComponentFactoryResolver,
  Injector,
  OnDestroy,
  OnInit, Type
} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {Subscription} from 'rxjs';
import {ClassroomService} from '../../service/classroom.service';
import {Ticket} from '../../model/Ticket';
import {User} from "../../model/User";
import {TicketService} from "../../service/ticket.service";
import {UserService} from "../../service/user.service";
import {ConferenceInfo} from "../../model/ConferenceInfo";
import {ClassroomInfo} from "../../model/ClassroomInfo";
import {UserListComponent} from "./user-list/user-list.component";
import {ConferenceListComponent} from "./conference-list/conference-list.component";

@Component({
  selector: 'app-classroom',
  templateUrl: './classroom.component.html',
  styleUrls: ['./classroom.component.scss']
})
export class ClassroomComponent implements OnInit, OnDestroy {

  currentUser: User | undefined
  users: User[] = [];
  tickets: Ticket[] = [];
  conferences: ConferenceInfo[] = [];
  classroomInfo: ClassroomInfo | undefined;
  subscriptions: Subscription[] = [];
  userListComponent: Type<UserListComponent> | undefined
  conferenceListComponent: Type<ConferenceListComponent> | undefined;

  constructor(public classroomService: ClassroomService,
              private ticketService: TicketService,
              private userService: UserService,
              private title: Title,
              private resolver: ComponentFactoryResolver) {
  }

  ngOnInit(): void {
    Notification.requestPermission().then();
    this.subscriptions.push(
      this.classroomService.currentUserObservable.subscribe(
      currentUser => this.currentUser = currentUser
      ),
      this.classroomService.tickets.subscribe(
      tickets => this.tickets = tickets
      ),
      this.classroomService.userDisplayObservable.subscribe(
      users => this.users = users
      ),
      this.classroomService.classroomInfoObservable.subscribe(classroomInfo => {
          this.classroomInfo = classroomInfo
          this.title.setTitle(`${classroomInfo.classroomName} | Digital Classroom`);
      })
    )
    this.createUserList();
    this.createConferenceList();
  }

  createUserList() {
    const factory: ComponentFactory<UserListComponent> = this.resolver.resolveComponentFactory(UserListComponent)
    const injector = Injector.create({providers: []});
    this.userListComponent = factory.create(injector).componentType
  }

  createConferenceList() {
    const factory: ComponentFactory<ConferenceListComponent> = this.resolver.resolveComponentFactory(ConferenceListComponent)
    const injector = Injector.create({providers: []});
    this.conferenceListComponent = factory.create(injector).componentType
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe())
  }

}
