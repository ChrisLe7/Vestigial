package es.uco.ism.servlet.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import es.uco.ism.business.user.UserDTO;
import es.uco.ism.data.UserDAO;
import es.uco.ism.display.UserBean;
import es.uco.ism.utils.PasswordHashing;
/**
 * Servlet implementation class LoginController
 */
public class LoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public LoginController() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		String url_bd = request.getServletContext().getInitParameter("URL");
		String username_bd = request.getServletContext().getInitParameter("USER");
		String password_bd = request.getServletContext().getInitParameter("PASSWORD");
		String sql = request.getServletContext().getInitParameter("sql");
		
		ServletContext application = getServletContext();
		InputStream myIO = application.getResourceAsStream(sql);
		java.util.Properties prop = new java.util.Properties();
		prop.load(myIO);
		
		UserBean usuario = (UserBean) session.getAttribute("userBean");
		
		Boolean login = usuario != null && !usuario.getEmail().equals("");
				
		RequestDispatcher disparador = null;
		UserDAO userDAO = new UserDAO (url_bd, username_bd, password_bd, prop);
		String nextPage ="View/Main/loginView.jsp"; 
		String mensajeNextPage = "";
		String dataJson = request.getReader().readLine();
		JSONObject objJson = null;
		String UserEmail;
		String UserPassword;			
		if (dataJson != null) {
			objJson = new JSONObject(dataJson);
			response.setContentType("application/json");
			JSONObject jsonDataEnviar = null;
			PrintWriter out = response.getWriter();
			jsonDataEnviar = new JSONObject();
			String mensajeResultado = null;
			if (!objJson.isEmpty()) {
				UserEmail = (String) objJson.get("email");
				UserPassword = (String) objJson.get("password");
				UserDTO userDTO = userDAO.QueryByEmail(UserEmail);
				if (userDTO == null) {
					mensajeResultado = "[ERROR] El usuario no existe";
				}else {
					String saltPassword = userDTO.getSalt();
					
					String passwordHash = PasswordHashing.createHash(UserPassword, saltPassword);
					
			
					if (passwordHash.equals(userDTO.getPwd())) {
						mensajeResultado = "[OK] Usuario y Password Correcta";
						ArrayList<UserDTO> usuarioInfo = new ArrayList<>();
						
						UserDTO usuarioAUX = new UserDTO (UserEmail, "", "", userDTO.getPhone(), userDTO.getPrefix(),userDTO.getGroups()); 
						usuarioInfo.add(usuarioAUX);
						jsonDataEnviar.put("user", usuarioInfo);
					}
					else {
						mensajeResultado = "[ERROR] Password Incorrecta";
					}
				}
				jsonDataEnviar.put("Mensaje", mensajeResultado);
				out.print(jsonDataEnviar);
				out.close();
			}
		}else {
			if (!login) {
				
				UserEmail = request.getParameter("email");
				UserPassword = request.getParameter("password");				
				
				if (UserEmail != null) {
					
					UserDTO userDTO = userDAO.QueryByEmail(UserEmail);
					
					if (userDTO == null) {
						//El usuario no existe 
						nextPage = "View/Main/loginView.jsp";
						mensajeNextPage = "Error de Usuario, Intentelo de Nuevo";
					}
					else {
						String saltPassword = userDTO.getSalt();
						
						String passwordHash = PasswordHashing.createHash(UserPassword, saltPassword);
						
				
						if (passwordHash.equals(userDTO.getPwd())) {
							usuario = new UserBean ();
							usuario.setEmail(UserEmail);
							session.setAttribute("userBean", usuario);
							nextPage = "/"; //mira redireccion
						}
						else {
							nextPage = "View/Main/loginView.jsp";
							mensajeNextPage = "Error de Contrase�a, Intentelo de Nuevo";
						}
					}
				}
				else {
					nextPage = "View/Main/loginView.jsp";
					mensajeNextPage = "Rellene los campos con su email y contrase�a para acceder";
				}
			}
			else {
				//Se encuentra logueado deber� de acceder al Home.
				//nextPage = "index.jsp";
				//disparador = request.getRequestDispatcher(nextPage);
				
				request.getSession().removeAttribute("usuario");
						
				
				nextPage = "/index.jsp";//mirar redireccion

				
			}
			disparador = request.getRequestDispatcher(nextPage);
			request.setAttribute("mensaje", mensajeNextPage);
			disparador.forward(request, response);
		}
		
	}

	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
