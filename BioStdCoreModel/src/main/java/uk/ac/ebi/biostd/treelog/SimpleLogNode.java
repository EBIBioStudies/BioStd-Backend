/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.treelog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SimpleLogNode implements LogNode, Serializable {

    private static final long serialVersionUID = 1L;

    private String nodeMessage;
    private Level level;

    private List<LogNode> subNodes;

    private transient ErrorCounter errCnt;

    SimpleLogNode() {
    }

    public SimpleLogNode(Level l, String msg, ErrorCounter rn) {
        nodeMessage = msg;
        level = l;
        errCnt = rn;
    }

    public static void setLevels(LogNode ln) {
        setLevels(ln, Level.getMinLevel());
    }

    public static void setLevels(LogNode ln, Level reqLevel) {
        if (ln.getSubNodes() == null) {
            if (ln.getLevel() == null) {
                ln.setLevel(Level.INFO);
            }

            return;
        }

        LogNode.Level maxLevel = ln.getLevel() != null ? ln.getLevel() : Level.getMinLevel();

        for (LogNode snd : ln.getSubNodes()) {
            setLevels(snd, reqLevel);

            if (snd.getLevel().getPriority() > maxLevel.getPriority()) {
                maxLevel = snd.getLevel();
            }
        }

        ln.setLevel(maxLevel);

        if (maxLevel.getPriority() >= reqLevel.getPriority()) {
            Iterator<? extends LogNode> lnIter = ln.getSubNodes().iterator();

            while (lnIter.hasNext()) {
                LogNode iln = lnIter.next();

                if (iln.getLevel().getPriority() < reqLevel.getPriority()) {
                    lnIter.remove();
                }
            }
        }
    }

    @Override
    public void log(Level lvl, String msg) {
        if (subNodes == null) {
            subNodes = new ArrayList<>(10);
        }

        subNodes.add(new SimpleLogNode(lvl, msg, errCnt));

        if (errCnt != null && lvl.getPriority() >= Level.ERROR.getPriority()) {
            errCnt.incErrorCounter();
        }
    }

    @Override
    public LogNode branch(String msg) {
        if (subNodes == null) {
            subNodes = new ArrayList<>(10);
        }

        LogNode nnd = new SimpleLogNode(Level.SUCCESS, msg, errCnt);

        subNodes.add(nnd);

        return nnd;
    }

    @Override
    public void append(LogNode node) {
        if (subNodes == null) {
            subNodes = new ArrayList<>(10);
        }

        subNodes.add(node);

        if (errCnt != null) {
            errCnt.addErrorCounter(countErrors(node));
        }
    }

    private int countErrors(LogNode node) {

        if (node.getSubNodes() == null) {
            if (node.getLevel().getPriority() >= Level.ERROR.getPriority()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            int res = 0;

            for (LogNode sn : node.getSubNodes()) {
                res += countErrors(sn);
            }

            return res;
        }
    }

    @Override
    public String getMessage() {
        return nodeMessage;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public void setLevel(Level l) {
        level = l;
    }

    @Override
    public List<LogNode> getSubNodes() {
        return subNodes;
    }

    @Override
    public void success() {
        level = Level.SUCCESS;
    }

    @Override
    public boolean remove(LogNode ln) {
        if (subNodes == null || !subNodes.remove(ln)) {
            return false;
        }

        if (errCnt != null) {
            errCnt.addErrorCounter(-countErrors(ln));
        }

        return true;
    }

    @Override
    public boolean move(LogNode oldPar, LogNode newPar) {
        if (oldPar instanceof SimpleLogNode && newPar instanceof SimpleLogNode) {
            if (((SimpleLogNode) oldPar).subNodes == null || !((SimpleLogNode) oldPar).subNodes.remove(this)) {
                return false;
            }

            if (((SimpleLogNode) newPar).subNodes == null) {
                ((SimpleLogNode) newPar).subNodes = new ArrayList<>(10);
            }

            ((SimpleLogNode) newPar).subNodes.add(this);
        } else {
            if (!oldPar.remove(this)) {
                return false;
            }

            newPar.append(this);
        }

        return true;
    }
}
