package ib.project.service;

import java.util.List;

import ib.project.model.User;

public interface UserServiceInterface {
	
	public User findById(int id); 
	
	public User findByEmail(String email);
	
	public List<User> findAll ();

	User save(User user);
}
