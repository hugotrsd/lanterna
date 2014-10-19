/*
 * This file is part of lanterna (http://code.google.com/p/lanterna/).
 * 
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2010-2014 Martin
 */
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

/**
 *
 * @author Martin
 */
public abstract class AbstractComponent implements Component {
    private Composite parent;
    private TerminalSize size;
    private TerminalSize explicitPreferredSize;   //This is keeping the value set by the user (if setPreferredSize() is used)
    private TerminalPosition position;
    private Object layoutData;
    private boolean invalid;
    private boolean disposed;

    public AbstractComponent() {
        size = TerminalSize.ZERO;
        position = TerminalPosition.TOP_LEFT_CORNER;
        explicitPreferredSize = null;
        layoutData = null;
        invalid = true;
        disposed = false;
        parent = null;
    }

    protected void invalidate() {
        ensureNotDisposed();
        invalid = true;
    }

    @Override
    public AbstractComponent setSize(TerminalSize size) {
        ensureNotDisposed();
        this.size = size;
        return this;
    }

    @Override
    public TerminalSize getSize() {
        return size;
    }

    @Override
    public final TerminalSize getPreferredSize() {
        if(explicitPreferredSize != null) {
            return explicitPreferredSize;
        }
        else {
            return calculatePreferredSize();
        }
    }

    @Override
    public final AbstractComponent setPreferredSize(TerminalSize explicitPreferredSize) {
        this.explicitPreferredSize = explicitPreferredSize;
        return this;
    }

    public abstract TerminalSize calculatePreferredSize();

    @Override
    public AbstractComponent setPosition(TerminalPosition position) {
        ensureNotDisposed();
        this.position = position;
        return this;
    }

    @Override
    public TerminalPosition getPosition() {
        return position;
    }
    
    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public final void draw(TextGUIGraphics graphics) {
        drawComponent(graphics);
        invalid = false;
    }

    /**
     * Implement this method to define the logic to draw the component. The reason for this abstract method, instead of
     * overriding {@code Component.draw(..)} is because {@code AbstractComponent.draw(..)} calls this method and then
     * resets the invalid flag. If you could override {@code draw}, you might forget to call the super method and
     * probably won't notice that your code keeps refreshing the GUI even though nothing has changed.
     * @param graphics TextGraphics to be used to draw the component
     */
    public abstract void drawComponent(TextGUIGraphics graphics);

    @Override
    public AbstractComponent setLayoutData(Object data) {
        ensureNotDisposed();
        if(layoutData != data) {
            layoutData = data;
            invalidate();
        }
        return this;
    }

    @Override
    public Object getLayoutData() {
        return layoutData;
    }

    @Override
    public Composite getParent() {
        return parent;
    }

    @Override
    public RootContainer getRootContainer() {
        if(parent == null) {
            return null;
        }
        return parent.getRootContainer();
    }

    @Override
    public TerminalPosition toRootContainer(TerminalPosition position) {
        return getParent().toRootContainer(getPosition().withRelative(position));
    }

    @Override
    public void setParent(Composite parent) {
        ensureNotDisposed();
        if(this.parent == parent) {
            return;
        }
        Composite oldParent = this.parent;
        this.parent = null;
        if(oldParent != null && oldParent.containsComponent(this)) {
            oldParent.removeComponent(this);
        }
        this.parent = parent;
        if(parent != null) {
            if(!parent.containsComponent(this)) {
                parent.addComponent(this);
            }
        }
    }

    @Override
    public Border withBorder(Border border) {
        border.addComponent(this);
        return border;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(!disposed) {
            dispose();
        }
    }

    @Override
    public final void dispose() {
        if(disposed) {
            //We could throw an error here but let's be nice
            return;
        }
        if(parent != null) {
            parent.removeComponent(this);
        }
        onDisposed();
        disposed = true;
    }

    protected void onDisposed() {
        //Available for custom implementation
    }

    protected void ensureNotDisposed() {
        if(disposed) {
            throw new IllegalStateException("Component " + toString() + " is already disposed");
        }
    }
}
