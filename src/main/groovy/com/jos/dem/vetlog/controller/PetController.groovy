package com.jos.dem.vetlog.controller

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.servlet.ModelAndView
import org.springframework.validation.BindingResult
import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

import com.jos.dem.vetlog.model.Pet
import com.jos.dem.vetlog.model.PetImage
import com.jos.dem.vetlog.model.PetType
import com.jos.dem.vetlog.model.User
import com.jos.dem.vetlog.command.Command
import com.jos.dem.vetlog.command.PetCommand
import com.jos.dem.vetlog.validator.PetValidator
import com.jos.dem.vetlog.service.BreedService
import com.jos.dem.vetlog.service.PetService
import com.jos.dem.vetlog.service.PetImageService
import com.jos.dem.vetlog.service.UserService
import com.jos.dem.vetlog.service.LocaleService
import com.jos.dem.vetlog.client.S3Writer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller
@RequestMapping("/pet")
class PetController {

  @Autowired
  BreedService breedService
  @Autowired
  PetService petService
  @Autowired
  PetImageService petImageService
  @Autowired
  UserService userService
  @Autowired
  LocaleService localeService
  @Autowired
  S3Writer s3Writer

  @Value('${breedsByTypeUrl}')
  String breedsByTypeUrl
  @Value('${bucketDestination}')
  String bucketDestination

  Logger log = LoggerFactory.getLogger(this.class)

  @InitBinder
  private void initBinder(WebDataBinder binder) {
    binder.addValidators(new PetValidator())
  }

  @RequestMapping(method = GET, value = "/create")
  ModelAndView create(){
    ModelAndView modelAndView = new ModelAndView('pet/create')
    Command petCommand = new PetCommand()
    modelAndView.addObject('petCommand', petCommand)
    fillModelAndView(modelAndView)
  }

  @Transactional
  @RequestMapping(method = POST, value = "/save")
  ModelAndView save(@Valid PetCommand petCommand, BindingResult bindingResult) {
    log.info "Creating pet: ${petCommand.name}"
    ModelAndView modelAndView = new ModelAndView('pet/create')
    if (bindingResult.hasErrors()) {
      modelAndView.addObject('petCommand', petCommand)
      return fillModelAndView(modelAndView)
    }
    User user = userService.getCurrentUser()
    Pet pet = petService.save(petCommand, user)
    PetImage petImage = petImageService.save(pet)
    s3Writer.uploadToBucket(bucketDestination, petImage.uuid, petCommand.image.getInputStream())
    modelAndView.addObject('message', localeService.getMessage('pet.created'))
    petCommand = new PetCommand()
    modelAndView.addObject('petCommand', petCommand)
    fillModelAndView(modelAndView)
  }

  ModelAndView fillModelAndView(ModelAndView modelAndView){
    modelAndView.addObject('breeds', breedService.getBreedsByType(PetType.DOG))
    modelAndView.addObject('breedsByTypeUrl', breedsByTypeUrl)
    modelAndView
  }

  @RequestMapping(method=RequestMethod.GET, value="/list")
  @ResponseBody
  def listByType(@RequestParam String type, HttpServletResponse response){
    log.info "Listing Pets by type: $type"

    response.addHeader("Allow-Control-Allow-Methods", "GET")
    response.addHeader("Access-Control-Allow-Origin", "*")
    breedService.getBreedsByType(PetType.getPetTypeByValue(type))
  }

}
