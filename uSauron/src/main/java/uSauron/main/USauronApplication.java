package uSauron.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import uSauron.collector.InfoCollector;
import uSauron.main.USauronApplication;

@SpringBootApplication
@ComponentScan(basePackages = {"uSauron"})
public class USauronApplication {
	
	
	public USauronApplication() {
		System.out.println();
		System.out.println("		   . \\   _,.--~=~\"~=~--.._   / .");
		System.out.println("		  ;  _.-\"  / \\ !   ! / \\  \"-._  .");
		System.out.println("		 / ,\"     / ,` .---. `, \\     \". \\");
		System.out.println("		/.'   `~  |   /:::::\\   |  ~`   '.\\");
		System.out.println("		\\`.  `~   |   \\:::::/   | ~`  ~ .'/");
		System.out.println("		 \\ `.  `~ \\ `, `~~~' ,` /   ~`.' /");
		System.out.println("		  .  \"-._  \\ / !   ! \\ /  _.-\"  .");
		System.out.println("		   ./    \"=~~.._  _..~~=`\"    \\.");
		System.out.println("	       (                                 ");
		System.out.println("	       )\\ )                              ");
		System.out.println("	   (  (()/(    )    (   (                ");
		System.out.println("	  ))\\  /(_))( /(   ))\\  )(    (    (     ");
		System.out.println("	 /((_)(_))  )(_)) /((_)(()\\   )\\   )\\ )  ");
		System.out.println("	(_))( / __|((_)_ (_))(  ((_) ((_) _(_/(  ");
		System.out.println("	| || |\\__ \\/ _` || || || '_|/ _ \\| ' \\)) ");
		System.out.println("	 \\_,_||___/\\__,_| \\_,_||_|  \\___/|_||_|  ");
	                                         

	}
	
	public static void main(String[] args) {
		SpringApplication.run(USauronApplication.class, args);
	}

}
