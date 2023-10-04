package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entites.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordencoder;
	
	@Autowired
	private UserRepository userRepository;
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title","Home Page - Smart Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title","About Page - Smart Manager");
		return "about";
	}
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title","register Page - Smart Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	//handler for register user
	
	@RequestMapping(value="/do_register",method = RequestMethod.POST)
	public String register(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value="agreement",defaultValue="false")boolean agreement,
			Model model,HttpSession session)
	{

     
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions..");
				throw new Exception("You have not agreed the terms and conditions..");
				
			}
			
			if(result1.hasErrors()) {
				System.out.println("ERROR"+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnable(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordencoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.println("User"+user);
			
			User result = this.userRepository.save(user);
			
			
			model.addAttribute("user",new User());
			
			session.setAttribute("message",new Message("Succesfully Register!","alert-success"));
			
			return "signup";
		}
		catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Something Went Wrong!"+e.getMessage(),"alert-danger"));
			
			
		}
		return "signup";
	}
	
	
	//Handler For Custom login
	
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","Login Page");
	
		return "login";
	}
	}
	



