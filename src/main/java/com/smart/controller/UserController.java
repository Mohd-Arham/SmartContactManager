package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	
	//method comman data user
	@ModelAttribute
	public void addCommanData(Model m,Principal principal) {
		 String name = principal.getName();		
		 //get the user using userdetails
		 
		 User user = this.userRepository.getUserByUserName(name);
			
		 m.addAttribute("user", user);
		
	}
	
	
	//home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) 
	{
	
		model.addAttribute("title","User Dashoboard");
		return "normal/user_dashboard";
	}
	
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			 Principal principal,HttpSession session) {
		
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		
		//processing upload file
		
		if(file.isEmpty()) {
			System.out.println("File Is Not Empty");
			contact.setImage("contact.png");
		}
		else {
			contact.setImage(file.getOriginalFilename());
			
			File savefile = new ClassPathResource("static/Img").getFile();
			
			Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Images Uploaded In DB");
		}
		
		user.getContacts().add(contact);
		contact.setUser(user);
		this.userRepository.save(user);
		
		System.out.println("Add Data Into DataBase");
		
		//message success
		session.setAttribute("message",new Message("Your Contact Is Added !","success"));
		
		}
		catch(Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			
			//error message
			session.setAttribute("message",new Message("Something Went Wrong ? Try Again","danger"));
		}
		return "normal/add_contact_form";
		
	}
	
	//show contacs handler
	//per pages 5 contacts
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		
		m.addAttribute("title","Show User Contacts ");
		
	//fetch user id jo login hai
		String username = principal.getName();
		
		User user = this.userRepository.getUserByUserName(username);
		
		//contacts ki list
		//pageanation 
		Pageable pageable = PageRequest.of(page,5);
    Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
    
    m.addAttribute("contacts", contacts);
    m.addAttribute("currentPage",page);
    m.addAttribute("totalPages", contacts.getTotalPages());
    
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactsDetail(@PathVariable("cId") Integer cid,Model model,Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		//
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		if(user.getId() == contact.getUser().getId()) 
            
		{
			model.addAttribute("model", contact);
		model.addAttribute("title","Contact Details");
		
		}
		return "normal/contact_detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,HttpSession session,Principal principal) {
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		//check
		try {
		String surname=principal.getName();
		User user = this.userRepository.getUserByUserName(surname);
		//remove
		
	
		if(user.getId()== contact.getUser().getId()) {
			
	    Path p=Paths.get("C:\\Users\\Admin\\Eclipse Projects\\SmartContactManager1\\target\\classes\\static\\Img\\"+contact.getImage());
	    Files.delete(p);
	    System.out.println("Image deleted Succesfully");
		this.contactRepository.delete(contact);
		session.setAttribute("message",new Message("Contact Deleted Succesfully...","success"));
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	//update controller handler
	
	@PostMapping("/update-Contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		
		m.addAttribute("title","Update Contacts");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//update contact handler data
	@RequestMapping(value = "/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model
			,HttpSession session,Principal principal)
	{
		try {
			Contact oldpick = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				//delete old pic
				
				File deletefile = new ClassPathResource("static/Img").getFile();
				File f=new File(deletefile, oldpick.getImage());
				f.delete();
				
				File savefile = new ClassPathResource("static/Img").getFile();
				
				Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				System.out.println("Images Uploaded In DB");	
				
				
			}
			else {
				 contact.setImage(oldpick.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);

			System.out.println("id"+contact.getcId());
			this.contactRepository.save(contact);
			
			session.setAttribute("message",new Message("Your contact is updated...","success"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//show user controller show profile user
	
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		
		m.addAttribute("title","Profile Page");
		
	return "normal/profile";	
	}
	
	
	//update user form details 
	
	@PostMapping("/update_user/{id}")
	public String UpdateForm(@PathVariable("id") Integer id,Model m) {
		
		m.addAttribute("title","Update User Details");
	User user = this.userRepository.findById(id).get();
	m.addAttribute("user", user);
		return "normal/update-user";
	}
	
	//update details user
	
	@PostMapping("/update-user")
	public String updateuser(@ModelAttribute User user,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session
			,Principal principal) {
		
		try {
			
			User userId = this.userRepository.findById(user.getId()).get();
			if(!file.isEmpty())
			{
				File deletefile=new ClassPathResource("static/Img").getFile();
				File f=new File(deletefile,userId.getImageUrl());
				f.delete();
				File saveFile=new ClassPathResource("static/Img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+ File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				user.setImageUrl(file.getOriginalFilename());
				System.out.println("Imaage Uplaod Successfully!");
				
			}
			else {
				user.setImageUrl(userId.getImageUrl());
				
			}
			
			System.out.println("userId"+user.getId());
			userRepository.save(user);
			session.setAttribute("message",new Message("Your contact is updated...","success"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/profile";
	}
	
	//open setting handler
	
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	//chnage password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldpassword") String oldpassword,@RequestParam("newpassword")
	    String newpassword,Principal principal,HttpSession session) 
	{
		String username = principal.getName();
		User currentName = this.userRepository.getUserByUserName(username);
		
		if(this.bCryptPasswordEncoder.matches(oldpassword,currentName.getPassword())) {
			//change password
			
	   currentName.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
	  this.userRepository.save(currentName);
	  session.setAttribute("message", new Message("Your Password is successfully changed...","success"));
	  
			
		}
		else {
			//error
			 session.setAttribute("message", new Message("Wrong Old Password Please Enter Correct Password","danger"));
			 return "redirect:/user/settings";  
		}
		
		return "redirect:/user/index";
		
	}
}
