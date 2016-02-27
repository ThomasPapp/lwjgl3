/*
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package org.lwjgl.demo.glfw;

import org.lwjgl.demo.opengl.AbstractGears;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.libffi.Closure;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/** The Gears demo implemented using GLFW. */
public class Gears extends AbstractGears {

	private GLFWErrorCallback      errorCB;
	private GLFWKeyCallback        keyCB;
	private GLFWWindowSizeCallback windowSizeCB;

	private Closure debugProc;

	private long window;

	private Boolean toggleMode;

	public static void main(String[] args) {
		new Gears().run();
	}

	private void run() {
		try {
			init();
			initGLState();

			loop();
		} finally {
			try {
				destroy();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void init() {
		errorCB = GLFWErrorCallback.createPrint().set();
		if ( glfwInit() != GLFW_TRUE )
			throw new IllegalStateException("Unable to initialize glfw");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

		keyCB = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if ( action != GLFW_RELEASE )
					return;

				switch ( key ) {
					case GLFW_KEY_ESCAPE:
						glfwSetWindowShouldClose(window, GLFW_TRUE);
						break;
					case GLFW_KEY_F:
						if ( glfwGetWindowMonitor(window) == NULL )
							toggleMode = true;
						break;
					case GLFW_KEY_W:
						if ( glfwGetWindowMonitor(window) != NULL )
							toggleMode = false;
						break;
					case GLFW_KEY_G:
						glfwSetInputMode(window, GLFW_CURSOR, glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_NORMAL
							? GLFW_CURSOR_DISABLED
							: GLFW_CURSOR_NORMAL
						);
						break;
				}
			}
		};

		windowSizeCB = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				glViewport(0, 0, width, height);
			}
		};

		createWindow(false);
	}

	private void createWindow(boolean fullscreen) {
		int WIDTH = 300;
		int HEIGHT = 300;

		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode vidmode = glfwGetVideoMode(monitor);

		long window = fullscreen
			? glfwCreateWindow(vidmode.width(), vidmode.height(), "GLFW Gears Demo", monitor, this.window)
			: glfwCreateWindow(WIDTH, HEIGHT, "GLFW Gears Demo", NULL, this.window);

		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetWindowSizeLimits(window, WIDTH, HEIGHT, GLFW_DONT_CARE, GLFW_DONT_CARE);
		glfwSetWindowAspectRatio(window, 1, 1);

		// Destroy old window
		if ( this.window != NULL ) {
			glfwSetInputMode(this.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			glfwDestroyWindow(this.window);
			if ( debugProc != null )
				debugProc.free();
		}

		keyCB.set(window);
		windowSizeCB.set(window);

		if ( fullscreen )
			glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		else {
			// Center window
			glfwSetWindowPos(
				window,
				(vidmode.width() - WIDTH) / 2,
				(vidmode.height() - HEIGHT) / 2
			);
		}

		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		debugProc = GLUtil.setupDebugMessageCallback();

		glfwSwapInterval(1);
		glfwShowWindow(window);

		this.window = window;
	}

	private void loop() {
		long lastUpdate = System.currentTimeMillis();
		int frames = 0;

		while ( glfwWindowShouldClose(window) == GLFW_FALSE ) {
			glfwPollEvents();

			if ( toggleMode != null ) {
				// Toggle between windowed and fullscreen modes
				createWindow(toggleMode);
				initGLState();
				toggleMode = null;
			}

			renderLoop();

			glfwSwapBuffers(window);

			frames++;

			long time = System.currentTimeMillis();
			int UPDATE_EVERY = 5; // seconds
			if ( UPDATE_EVERY * 1000L <= time - lastUpdate ) {
				lastUpdate = time;

				System.out.printf("%d frames in %d seconds = %.2f fps\n", frames, UPDATE_EVERY, (frames / (float)UPDATE_EVERY));
				frames = 0;
			}
		}
	}

	private void destroy() {
		if ( debugProc != null )
			debugProc.free();

		if ( window != NULL ) {
			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
		}

		glfwTerminate();

		if ( errorCB != null )
			errorCB.free();
	}

}