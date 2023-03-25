package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectLoggedUser;
import cz.tstrecha.timetracker.annotation.PermissionCheck;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.TaskFilter;
import cz.tstrecha.timetracker.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "task-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "task", produces = {APPLICATION_JSON_VALUE})
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PermissionCheck("task.create")
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskCreateRequestDTO task, @InjectLoggedUser LoggedUser user){
        return new ResponseEntity<>(taskService.createTask(task,user), HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public void createTasks(@InjectLoggedUser LoggedUser user){
        Random random = new Random();
        String[] names = {"Responsive Web Design",
                "RESTful API Development",
                "API Integration",
                "Software Debugging",
                "Mobile App Optimization",
                "Database Schema Implementation",
                "Automated Testing",
                "User Authentication",
                "Custom WordPress Plugin Development",
                "Machine Learning Algorithm Development",
                "Payment Gateway Integration",
                "Chatbot Development",
                "Data Visualization Dashboard Creation",
                "Web Scraping",
                "Native iOS App Development",
                "Android App Development",
                "E-commerce Website Development",
                "Social Media Integration",
                "Augmented Reality Development",
                "Virtual Reality Development",
                "Cross-platform App Development",
                "Blockchain Development",
                "Cloud Computing Integration",
                "Artificial Intelligence Integration",
                "Gaming App Development",
                "Internet of Things Integration",
                "Cybersecurity Implementation",
                "CRM Software Development",
                "Content Management System Development",
                "Geolocation-based App Development",
                "Wearable App Development",
                "Big Data Analysis",
                "Real-time Chat Application Development",
                "Browser Extension Development",
                "Responsive Email Template Development",
                "Mobile Game Development",
                "Voice Assistant Integration",
                "Progressive Web App Development",
                "Website Localization",
                "Video Streaming Platform Development",
                "Serverless Architecture Implementation",
                "Cloud Infrastructure Setup",
                "Search Engine Optimization",
                "UI/UX Design",
                "Mobile-first Design",
                "Digital Marketing Automation",
                "Web Analytics Integration",
                "Cross-browser Compatibility Testing",
                "Mobile Device Testing",
                "Accessibility Testing",
                "Usability Testing",
                "Code Refactoring",
                "Code Review",
                "DevOps Automation",
                "Continuous Integration/Continuous Delivery",
                "Microservices Architecture",
                "Containerization",
                "Server Administration",
                "IT Infrastructure Management",
                "Technical Documentation",
                "Agile Project Management",
                "Quality Assurance Testing",
                "Frontend Development"};
        String[] descriptions = {"Create a responsive design for our web application.",
                "Develop a RESTful API for our mobile app.",
                "Write code to integrate two APIs.",
                "Debug and fix a critical issue in our software.",
                "Optimize the performance of our mobile app.",
                "Implement a database schema for our new project.",
                "Write automated tests for our software.",
                "Add user authentication to our web application.",
                "Create a custom WordPress plugin.",
                "Develop a machine learning algorithm for our data analysis platform.",
                "Integrate a payment gateway into our e-commerce platform.",
                "Build a chatbot for our customer service system.",
                "Create a data visualization dashboard.",
                "Write code to scrape data from a website.",
                "Develop a native mobile app for iOS.",
                "Implement a caching layer for our database.",
                "Add multi-language support to our website.",
                "Write a custom Shopify app.",
                "Develop a recommendation engine for our e-commerce platform.",
                "Create a custom Drupal module.",
                "Implement search functionality for our website.",
                "Write a script to automate a repetitive task.",
                "Build a responsive email template.",
                "Integrate a social media API into our platform.",
                "Create a custom Google Analytics dashboard.",
                "Develop a plugin for our data visualization tool.",
                "Optimize the SEO of our website.",
                "Create a mobile-first design for our new feature.",
                "Write a script to automate data entry.",
                "Develop a responsive layout for our e-commerce platform.",
                "Design a custom icon set for our mobile app.",
                "Integrate a chatbot into our customer support system.",
                "Create a custom JavaScript library.",
                "Write code to automate testing of our software.",
                "Develop a custom email marketing template.",
                "Implement a recommendation engine for our content management system.",
                "Create a custom theme for our e-commerce platform.",
                "Write a script to import data from a CSV file.",
                "Build a responsive landing page for our marketing campaign.",
                "Implement a real-time notification system for our platform.",
                "Create a custom plugin for our analytics tool.",
                "Develop a custom dashboard for our platform.",
                "Write a script to automate deployment of our software.",
                "Design a new user interface for our mobile app.",
                "Optimize the speed of our website.",
                "Implement a commenting system for our blog.",
                "Create a custom form builder for our platform.",
                "Write code to automate report generation.",
                "Develop a custom theme for our content management system.",
                "Implement a ratings and reviews system for our e-commerce platform.",
                "Create a custom checkout process for our e-commerce platform.",
                "Develop a custom plugin for our e-learning platform.",
                "Write code to automate data analysis.",
                "Design a custom logo for our company.",
                "Implement a newsletter subscription form on our website.",
                "Create a custom navigation menu for our website.",
                "Develop a custom widget for our platform.",
                "Optimize the performance of our database.",
                "Implement a custom search engine for our platform.",
                "Write a script to automate backups of our data.",
                "Develop a custom theme for our forum.",
                "Create a custom error page for our website.",
                "Implement a multi-step checkout process for our e-commerce platform."};
        String[] notes = {
                "Design websites that work well on various devices and screen sizes",
                "Develop APIs that follow the REST architecture for efficient communication between servers and clients",
                "Integrate third-party APIs with existing systems",
                "Find and fix bugs in software applications",
                "Optimize mobile apps for performance and user experience",
                "Create efficient database schemas for data storage and retrieval",
                "Automate testing processes to improve software quality",
                "Implement secure user authentication mechanisms",
                "Develop custom WordPress plugins to extend functionality",
                "Develop machine learning algorithms to solve complex problems",
                "Integrate payment gateways to facilitate online transactions",
                "Build chatbots to automate customer support",
                "Create data visualization dashboards to present complex data in an understandable way",
                "Extract data from websites using web scraping tools",
                "Develop native iOS apps using Swift or Objective-C",
                "Build Android apps using Java or Kotlin",
                "Create e-commerce websites with online shopping carts and payment gateways",
                "Integrate social media features into web and mobile apps",
                "Develop augmented reality applications for smartphones and tablets",
                "Build virtual reality applications for head-mounted displays",
                "Develop cross-platform mobile apps using frameworks like React Native or Xamarin",
                "Build decentralized apps using blockchain technology",
                "Integrate cloud computing services like AWS or Azure into software applications",
                "Integrate artificial intelligence and machine learning models into software applications",
                "Develop gaming apps for mobile and desktop platforms",
                "Integrate Internet of Things (IoT) devices into software applications",
                "Implement cybersecurity measures to protect software applications and data",
                "Develop CRM software to manage customer relationships",
                "Develop content management systems for websites and mobile apps",
                "Build geolocation-based mobile apps to provide location-based services",
                "Develop wearable apps for smartwatches and other wearables",
                "Analyze large datasets to derive insights and make data-driven decisions",
                "Develop real-time chat applications for web and mobile platforms",
                "Build browser extensions to add functionality to web browsers",
                "Create responsive email templates that work well on various devices and email clients",
                "Develop mobile games for iOS and Android platforms",
                "Integrate voice assistants like Alexa or Siri into software applications",
                "Build progressive web apps that work like native apps on mobile devices",
                "Localize websites and mobile apps to support multiple languages and cultures",
                "Develop video streaming platforms for live and recorded video content",
                "Implement serverless architecture using services like AWS Lambda or Azure Functions",
                "Set up cloud infrastructure using services like AWS or Azure",
                "Optimize websites and mobile apps for search engines",
                "Design user interfaces and user experiences for software applications",
                "Design mobile-first user interfaces that work well on small screens",
                "Automate digital marketing campaigns to save time and improve efficiency",
                "Integrate web analytics tools like Google Analytics into websites and mobile apps",
                "Test websites and mobile apps for cross-browser compatibility issues",
                "Test mobile apps on various devices and platforms",
                "Test software applications for accessibility compliance",
                "Test software applications for usability and user experience",
                "Refactor existing code to improve performance and maintainability",
                "Review code written by other developers to improve quality and maintainability",
                "Automate software development processes using tools like Jenkins or CircleCI",
                "Set up continuous integration and continuous delivery pipelines to improve software delivery",
                "Design software architectures using microservices",
                "Containerize software applications using tools like Docker or Kubernetes",
                "Administer servers and manage server infrastructure using tools like Ansible or Terraform",
                "Manage IT infrastructure using tools like AWS or Azure",
                "Create technical documentation for software applications",
                "Manage software development projects using agile methodologies",
                "Test software applications for quality and accuracy",
                "Develop front-end user interfaces for websites and web applications"
        };

        for (int i = 0; i < names.length; i++) {
            TaskCreateRequestDTO task = new TaskCreateRequestDTO();
            task.setId(0L);
            task.setCustomId(random.nextLong(9999));
            task.setName(names[i]);
            task.setNameSimple(null);
            task.setNote(notes[i]);
            task.setDescription(descriptions[i]);
            task.setStatus(TaskStatus.values()[random.nextInt(TaskStatus.values().length)]);
            task.setEstimate(random.nextLong(9999));
            task.setActive(random.nextBoolean());
            taskService.createTask(task, user);
        }
    }

    @PostMapping("/{identifier}/{identifierValue}")
    public ResponseEntity<TaskDTO> createEmptyTask(@PathVariable IdentifierType identifier,
                                                   @PathVariable String identifierValue,
                                                   @InjectLoggedUser LoggedUser user){
        return new ResponseEntity<>(taskService.createEmptyTask(identifier,identifierValue,user), HttpStatus.CREATED);
    }

    @PutMapping
    @PermissionCheck("task.update")
    public ResponseEntity<TaskDTO> updateTask(@RequestBody TaskCreateRequestDTO taskCreateRequestDTO,
                                              @InjectLoggedUser LoggedUser loggedUser){
        return new ResponseEntity<>(taskService.updateTask(taskCreateRequestDTO, loggedUser), HttpStatus.OK);
    }

    @PatchMapping("/{id}/{newStatus}")
    @PermissionCheck("task.update")
    public ResponseEntity<TaskDTO> changeTaskStatus(@PathVariable Long id,
                                                    @PathVariable TaskStatus newStatus,
                                                    @InjectLoggedUser LoggedUser loggedUser){
        return new ResponseEntity<>(taskService.changeTaskStatus(id, newStatus, loggedUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PermissionCheck("task.update")
    public ResponseEntity<TaskDTO> deleteTask(@PathVariable Long id, @InjectLoggedUser LoggedUser loggedUser){
        return new ResponseEntity<>(taskService.deleteTask(id, loggedUser), HttpStatus.OK);
    }

    @PatchMapping("/{id}/reactivate")
    @PermissionCheck("task.update")
    public ResponseEntity<TaskDTO> reactivateTask(@PathVariable Long id, @InjectLoggedUser LoggedUser loggedUser){
        return new ResponseEntity<>(taskService.reactivateTask(id, loggedUser), HttpStatus.OK);
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<TaskDTO>> searchForTasks(@PathVariable String query,
                                                     @RequestParam(defaultValue = "5", required = false) Long limit,
                                                     @InjectLoggedUser LoggedUser user){
        return new ResponseEntity<>(taskService.searchForTasks(query,limit,user), HttpStatus.OK);
    }

    @PostMapping("/list")
    @PermissionCheck("task.read")
    public ResponseEntity<List<TaskDTO>> listTasks(@RequestBody TaskFilter taskFilter, @InjectLoggedUser LoggedUser loggedUser){
        return new ResponseEntity<>(taskService.listTasks(taskFilter, loggedUser), HttpStatus.OK);
    }
}
