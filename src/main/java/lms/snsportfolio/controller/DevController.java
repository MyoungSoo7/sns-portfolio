package lms.snsportfolio.controller;


import lms.snsportfolio.model.AlarmArgs;
import lms.snsportfolio.model.AlarmEvent;
import lms.snsportfolio.model.AlarmType;
import lms.snsportfolio.model.entity.UserEntity;
import lms.snsportfolio.producer.AlarmProducer;
import lms.snsportfolio.repository.UserEntityRepository;
import lms.snsportfolio.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-dev/v1")
@RequiredArgsConstructor
public class DevController {

    private final AlarmService notificationService;
    private final UserEntityRepository userEntityRepository;
    private final AlarmProducer alarmProducer;

    @GetMapping("/notification")
    public void test() {
        UserEntity entity = userEntityRepository.findById(5).orElseThrow();
        notificationService.send(AlarmType.NEW_LIKE_ON_POST, new AlarmArgs(0, 0), entity.getId());
    }

    @GetMapping("/send")
    public void send() {
        alarmProducer.send(new AlarmEvent(AlarmType.NEW_LIKE_ON_POST, new AlarmArgs(0, 0), 5));
    }

}
