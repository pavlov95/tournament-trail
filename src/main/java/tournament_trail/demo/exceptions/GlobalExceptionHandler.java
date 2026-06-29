package tournament_trail.demo.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ModelAndView handleInvalidVerificationToken(InvalidVerificationTokenException exception) {
        return new ModelAndView("redirect:/verification-failed?reason=invalid");
    }

    @ExceptionHandler(ExpiredVerificationTokenException.class)
    public ModelAndView handleExpiredVerificationToken(ExpiredVerificationTokenException exception) {
        return new ModelAndView("redirect:/verification-failed?reason=expired");
    }

}
