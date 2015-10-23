package cs499.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs499.exceptions.ItemProcessException;

/**
 * Servlet implementation class Servlet
 */
@WebServlet("/JavaControllerServlet")
public class JavaControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	BlackboardHandler bbHandler;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JavaControllerServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		bbHandler = (BlackboardHandler) request.getSession().getAttribute("bbHandler");
		try {
			bbHandler.buyItem(request.getParameter("name"));
			if(!bbHandler.hasItem(request.getParameter("name"))){
				throw new ItemProcessException("Item Purchase Failed.");
			}
		}catch (ItemProcessException e) {
			e.printStackTrace();
		}
	}

}
