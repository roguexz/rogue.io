/*
 * Copyright 2013, Rogue.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rogue.app.framework.scripting.view.bean;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple bean for executing a script on the server side.
 */
@Named
@ViewScoped
public class ScriptExecutor
{
    private boolean useRhino;
    private String engineName = "javascript";
    private String script;
    private String output;
    private List<String> availableScriptEngines;
    private transient ScriptEngineManager scriptEngineManager;

    /**
     * Get the scripting engine to use for executing the script.
     *
     * @return the scripting engine to use for executing the script.
     */
    public String getEngineName()
    {
        return engineName;
    }

    /**
     * Set the scripting engine to use for executing the script.
     *
     * @param engineName the scripting engine to use for executing the script.
     */
    public void setEngineName(String engineName)
    {
        this.engineName = engineName;
    }

    /**
     * Get a list of available scripting engines for executing scripts.
     *
     * @return a list of available scripting engines for executing scripts.
     */
    public List<String> getAvailableScriptEngines()
    {
        if (availableScriptEngines == null)
        {
            availableScriptEngines = new ArrayList<>();
            ScriptEngineManager manager = getScriptEngineManager();
            if (manager != null)
            {
                List<ScriptEngineFactory> factoryList = manager.getEngineFactories();
                for (ScriptEngineFactory factory : factoryList)
                {
                    availableScriptEngines.addAll(factory.getNames());
                }
            }

            if (availableScriptEngines.isEmpty())
            {
                useRhino = true;
                engineName = "Rhino";
                availableScriptEngines.add(engineName);
            }
        }
        return availableScriptEngines;
    }

    /**
     * Get the script to execute.
     *
     * @return the script to execute.
     */
    public String getScript()
    {
        return script;
    }

    /**
     * Set the script to execute.
     *
     * @param script the script to execute.
     */
    public void setScript(String script)
    {
        this.script = script;
    }

    /**
     * Get the output of the executed script.
     *
     * @return the output of the executed script.
     */
    public String getOutput()
    {
        return output;
    }

    /**
     * Execute the given script.
     */
    public String invokeAction()
    {

        if (script != null)
        {
            try
            {
                if (useRhino)
                {
                    output = Context.toString(executeUsingRhino(script));
                }
                else
                {
                    Object ret = executeUsingScriptEngine(script);
                    if (ret != null)
                    {
                        output = ret.toString();
                    }
                }
            }
            catch (Exception e)
            {
                StringWriter sw = new StringWriter(100);
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                output = sw.toString();
                output = output.replaceAll("\n", "<br>");
            }
        }
        return null;
    }

    private Object executeUsingRhino(String script) throws Exception
    {
        Context ctx = Context.enter();
        try
        {
            Scriptable scope = ctx.initStandardObjects();
            return ctx.evaluateString(scope, script, "<cmd>", 1, null);
        }
        finally
        {
            Context.exit();
        }
    }

    private Object executeUsingScriptEngine(String script) throws Exception
    {
        Object ret = null;
        ScriptEngineManager manager = getScriptEngineManager();

        ScriptEngine engine = manager.getEngineByName(getEngineName());
        if (engine != null)
        {
            ret = engine.eval(script);
        }
        else
        {
            ret = "Script engine is null. Cannot execute script.";
        }
        return ret;
    }

    /**
     * Get an instance of <code>ScriptEngineManager</code>.
     *
     * @return an instance of the script engine manager.
     */
    private ScriptEngineManager getScriptEngineManager()
    {
        if (scriptEngineManager == null)
        {
            scriptEngineManager = new ScriptEngineManager();
        }

        return scriptEngineManager;
    }

}
