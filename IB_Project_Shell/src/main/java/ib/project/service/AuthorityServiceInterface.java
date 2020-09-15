package ib.project.service;

import org.springframework.beans.factory.annotation.Autowired;

import ib.project.model.Authority;
import ib.project.repository.AuthorityRepository;

public interface AuthorityServiceInterface {
	
	public Authority findById(int id);
	
	public Authority findByName(String name);

}
